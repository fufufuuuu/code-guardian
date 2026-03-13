package com.codeguardian.application;

import com.codeguardian.config.AppConfig;
import com.codeguardian.domain.model.CodeChunk;
import com.codeguardian.domain.model.Issue;
import com.codeguardian.domain.model.ReviewTask;
import com.codeguardian.domain.review.IssueAggregator;
import com.codeguardian.domain.review.ReviewEngine;
import com.codeguardian.infrastructure.ai.LLMService;
import com.codeguardian.infrastructure.comment.CommentService;
import com.codeguardian.infrastructure.git.GitClient;
import com.codeguardian.infrastructure.git.GiteeClient;
import com.codeguardian.infrastructure.git.GithubClient;
import com.codeguardian.infrastructure.git.PRFile;
import com.codeguardian.infrastructure.parser.ChunkSplitter;
import com.codeguardian.infrastructure.parser.DiffParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 审查调度器
 * 负责控制整个审查流程，包括并发处理、限流和异常处理
 */
@Service
public class ReviewOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(ReviewOrchestrator.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 2000;

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private DiffParser diffParser;
    @Autowired
    private ChunkSplitter chunkSplitter;
    @Autowired
    private ReviewEngine reviewEngine;
    @Autowired
    private IssueAggregator issueAggregator;
    @Autowired
    private CommentService commentService;

    /**
     * 执行审查任务
     * @param task 审查任务
     * @throws Exception 异常
     */
    public void review(ReviewTask task) throws Exception {
        logger.info("Starting review orchestration for task: {}", task);

        try {
            // 1. 选择 Git 客户端
            GitClient gitClient = selectGitClient(task.getPlatform());
            commentService.setGitClient(gitClient);

            // 2. 获取 PR 文件
            List<PRFile> files = retry(() -> gitClient.getPullRequestFiles(task.getRepo(), task.getPrNumber()), "Failed to get PR files");
            logger.info("Found {} files in PR #{}", files.size(), task.getPrNumber());

            if (files.isEmpty()) {
                logger.info("No files found in PR #{}, skipping review", task.getPrNumber());
                return;
            }

            // 3. 解析 diff 生成 CodeChunk
            List<CodeChunk> chunks = diffParser.parse(files);
            logger.info("Generated {} code chunks from PR files", chunks.size());

            if (chunks.isEmpty()) {
                logger.info("No code chunks generated, skipping review");
                return;
            }

            // 4. 分割大的 CodeChunk
            List<CodeChunk> splitChunks = splitCodeChunks(chunks);
            logger.info("Split into {} chunks after splitting", splitChunks.size());

            // 5. 执行规则审查
            List<Issue> ruleIssues = reviewEngine.checkRules(splitChunks);
            logger.info("Found {} rule issues", ruleIssues.size());

            // 6. 并发执行 AI 审查
            List<Issue> aiIssues = reviewEngine.reviewAsync(splitChunks);
            logger.info("Found {} AI issues", aiIssues.size());

            // 7. 聚合问题
            List<Issue> issues = issueAggregator.aggregate(ruleIssues, aiIssues);
            logger.info("Aggregated {} issues after filtering", issues.size());

            // 8. 发布评论
            if (!issues.isEmpty()) {
                retry(() -> {
                    commentService.publish(task.getRepo(), task.getPrNumber(), issues);
                    return null;
                }, "Failed to publish comment");
                logger.info("Successfully published comment with {} issues to PR #{}", issues.size(), task.getPrNumber());
            } else {
                logger.info("No issues found, skipping comment publication");
            }

        } catch (Exception e) {
            logger.error("Error orchestrating review: {}", e.getMessage(), e);
            throw new Exception("Failed to orchestrate review: " + e.getMessage(), e);
        }
    }

    /**
     * 分割代码块
     * @param chunks 原始代码块列表
     * @return 分割后的代码块列表
     */
    private List<CodeChunk> splitCodeChunks(List<CodeChunk> chunks) {
        List<CodeChunk> splitChunks = new ArrayList<>();

        for (CodeChunk chunk : chunks) {
            try {
                List<CodeChunk> chunkParts = chunkSplitter.split(chunk);
                splitChunks.addAll(chunkParts);
            } catch (Exception e) {
                logger.warn("Error splitting chunk for file {}: {}", chunk.getFilePath(), e.getMessage());
                // 如果分割失败，使用原始 chunk
                splitChunks.add(chunk);
            }
        }

        return splitChunks;
    }

    /**
     * 根据平台选择 Git 客户端
     * @param platform 平台名称
     * @return Git 客户端
     */
    private GitClient selectGitClient(String platform) {
        switch (platform.toLowerCase()) {
            case "gitee":
                logger.info("Selected Gitee Git client");
                return new GiteeClient(appConfig);
            case "github":
                logger.info("Selected GitHub Git client");
                return new GithubClient(appConfig);
            default:
                logger.warn("Unknown platform '{}', defaulting to Gitee Git client", platform);
                return new GiteeClient(appConfig);
        }
    }

    /**
     * 重试机制
     * @param supplier 任务
     * @param errorMessage 错误信息
     * @param <T> 返回类型
     * @return 任务结果
     * @throws Exception 异常
     */
    private <T> T retry(RetryableTask<T> supplier, String errorMessage) throws Exception {
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                return supplier.execute();
            } catch (Exception e) {
                attempts++;
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    logger.error("{} after {} attempts: {}", errorMessage, MAX_RETRY_ATTEMPTS, e.getMessage());
                    throw new Exception(errorMessage + ": " + e.getMessage(), e);
                }
                long backoff = INITIAL_BACKOFF_MS * (1L << (attempts - 1)); // 指数退避
                logger.warn("{} (attempt {} of {}), retrying in {}ms: {}", errorMessage, attempts, MAX_RETRY_ATTEMPTS, backoff, e.getMessage());
                Thread.sleep(backoff);
            }
        }
        throw new Exception(errorMessage + ": Max retries exceeded");
    }

    /**
     * 可重试任务接口
     * @param <T> 返回类型
     */
    @FunctionalInterface
    private interface RetryableTask<T> {
        T execute() throws Exception;
    }
}
