package com.codeguardian.application;

import com.codeguardian.config.AppConfig;
import com.codeguardian.domain.model.CodeChunk;
import com.codeguardian.domain.model.Issue;
import com.codeguardian.domain.model.ReviewTask;
import com.codeguardian.domain.review.ReviewEngine;
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

/**
 * 审查服务
 * 系统的核心服务，负责协调整个代码审查流程
 * @author renlong.fu
 */
@Service
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    private GitClient gitClient;
    @Autowired
    private DiffParser diffParser;
    @Autowired
    private ChunkSplitter chunkSplitter;
    @Autowired
    private ReviewEngine reviewEngine;
    @Autowired
    private CommentService commentService;
    @Autowired
    private AppConfig appConfig;
    

    
    /**
     * 处理审查任务
     * @param task 审查任务
     * @throws Exception 异常
     */
    public void processTask(ReviewTask task) throws Exception {
        logger.info("Processing review task: {}", task);
        
        try {
            // 根据平台选择Git客户端
            selectGitClient(task.getPlatform());
            
            // 获取PR文件
            List<PRFile> prFiles = gitClient.getPullRequestFiles(task.getRepo(), task.getPrNumber());
            logger.info("Found {} files in PR #{}", prFiles.size(), task.getPrNumber());
            
            if (prFiles.isEmpty()) {
                logger.info("No files found in PR #{}, skipping review", task.getPrNumber());
                return;
            }
            
            // 解析diff生成CodeChunk
            List<CodeChunk> chunks = diffParser.parse(prFiles);
            logger.info("Generated {} code chunks from PR files", chunks.size());
            
            if (chunks.isEmpty()) {
                logger.info("No code chunks generated, skipping review");
                return;
            }
            
            // 分割大的CodeChunk
            List<CodeChunk> splitChunks = splitCodeChunks(chunks);
            logger.info("Split into {} chunks after splitting", splitChunks.size());
            
            // 执行代码审查
            List<Issue> issues = reviewEngine.reviewBatch(splitChunks);
            logger.info("Found {} issues in code review", issues.size());
            
            // 发布评论
            if (!issues.isEmpty()) {
                commentService.publish(task.getRepo(), task.getPrNumber(), issues);
                logger.info("Successfully published comment with {} issues to PR #{}", issues.size(), task.getPrNumber());
            } else {
                logger.info("No issues found, skipping comment publication");
            }
        } catch (Exception e) {
            logger.error("Error processing review task: {}", e.getMessage(), e);
            throw new Exception("Failed to process review task: " + e.getMessage(), e);
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
                // 如果分割失败，使用原始chunk
                splitChunks.add(chunk);
            }
        }
        
        return splitChunks;
    }
    
    /**
     * 根据平台选择Git客户端
     * @param platform 平台名称
     */
    private void selectGitClient(String platform) {
        switch (platform.toLowerCase()) {
            case "gitee":
                this.gitClient = new GiteeClient(appConfig);
                logger.info("Selected Gitee Git client");
                break;
            case "github":
                this.gitClient = new GithubClient(appConfig);
                logger.info("Selected GitHub Git client");
                break;
            default:
                this.gitClient = new GiteeClient(appConfig);
                logger.warn("Unknown platform '{}', defaulting to Gitee Git client", platform);
                break;
        }
        
        // 更新CommentService中的Git客户端
        commentService.setGitClient(gitClient);
    }
}