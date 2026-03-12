package com.codeguardian.api;

import com.codeguardian.api.response.ApiResponse;
import com.codeguardian.application.ReviewService;
import com.codeguardian.domain.model.ReviewTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Webhook控制器
 * 系统的入口点，负责接收PR webhook并触发审查流程
 * @author renlong.fu
 */
@RestController
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    @Autowired
    private ReviewService reviewService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    
    public WebhookController() {
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(5);
    }
    
    /**
     * 处理Gitee webhook
     * @param payload webhook payload
     * @param xGiteeToken Gitee webhook token
     * @return 响应
     */
    @PostMapping("/webhook/gitee")
    public ApiResponse handleGiteeWebhook(@RequestBody String payload, @RequestHeader(value = "X-Gitee-Token", required = false) String xGiteeToken) {
        try {
            logger.info("Received Gitee webhook");
            
            // 验证token（可选，根据实际配置）
            // validateToken(xGiteeToken);
            
            // 解析payload
            JsonNode rootNode = objectMapper.readTree(payload);
            
            // 提取事件类型
            String eventType = rootNode.path("action").asText();
            logger.info("Gitee webhook event: {}", eventType);
            
            // 只处理PR相关事件
            if (!isPullRequestEvent(rootNode)) {
                logger.info("Not a pull request event, skipping");
                return ApiResponse.success("Not a pull request event");
            }
            
            // 提取PR信息
            ReviewTask task = parseGiteeWebhookPayload(rootNode);
            if (task == null) {
                logger.error("Failed to parse Gitee webhook payload");
                return ApiResponse.error("Failed to parse webhook payload");
            }
            
            logger.info("Created review task: {}", task);
            
            // 异步处理审查任务
            executorService.submit(() -> {
                try {
                    reviewService.processTask(task);
                    logger.info("Successfully processed review task for PR #{}", task.getPrNumber());
                } catch (Exception e) {
                    logger.error("Error processing Gitee webhook: {}", e.getMessage(), e);
                }
            });
            
            return ApiResponse.success("Webhook received and processing started");
        } catch (Exception e) {
            logger.error("Error handling Gitee webhook: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to process webhook");
        }
    }
    
    /**
     * 处理GitHub webhook
     * @param payload webhook payload
     * @param xGithubEvent GitHub event type
     * @return 响应
     */
    @PostMapping("/webhook/github")
    public ApiResponse handleGitHubWebhook(@RequestBody String payload, @RequestHeader(value = "X-GitHub-Event", required = false) String xGithubEvent) {
        try {
            logger.info("Received GitHub webhook");
            
            // 提取事件类型
            String eventType = xGithubEvent != null ? xGithubEvent : "unknown";
            logger.info("GitHub webhook event: {}", eventType);
            
            // 只处理PR相关事件
            if (!"pull_request".equals(eventType)) {
                logger.info("Not a pull request event, skipping");
                return ApiResponse.success("Not a pull request event");
            }
            
            // 解析payload
            JsonNode rootNode = objectMapper.readTree(payload);
            
            // 提取PR信息
            ReviewTask task = parseGithubWebhookPayload(rootNode);
            if (task == null) {
                logger.error("Failed to parse GitHub webhook payload");
                return ApiResponse.error("Failed to parse webhook payload");
            }
            
            logger.info("Created review task: {}", task);
            
            // 异步处理审查任务
            executorService.submit(() -> {
                try {
                    reviewService.processTask(task);
                    logger.info("Successfully processed review task for PR #{}", task.getPrNumber());
                } catch (Exception e) {
                    logger.error("Error processing GitHub webhook: {}", e.getMessage(), e);
                }
            });
            
            return ApiResponse.success("Webhook received and processing started");
        } catch (Exception e) {
            logger.error("Error handling GitHub webhook: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to process webhook");
        }
    }
    
    /**
     * 验证webhook token
     * @param token 传入的token
     */
    private void validateToken(String token) {
        // 这里可以添加token验证逻辑
        // 例如：从配置中获取期望的token并进行比较
    }
    
    /**
     * 判断是否为PR事件
     * @param rootNode JSON根节点
     * @return 是否为PR事件
     */
    private boolean isPullRequestEvent(JsonNode rootNode) {
        return rootNode.has("pull_request");
    }
    
    /**
     * 解析Gitee webhook payload
     * @param rootNode JSON根节点
     * @return ReviewTask对象
     */
    private ReviewTask parseGiteeWebhookPayload(JsonNode rootNode) {
        try {
            JsonNode prNode = rootNode.path("pull_request");
            JsonNode repoNode = rootNode.path("repository");
            
            ReviewTask task = new ReviewTask();
            task.setPlatform("gitee");
            
            // 提取仓库信息
            String repoFullName = repoNode.path("full_name").asText();
            task.setRepo(repoFullName);
            
            // 提取PR编号
            int prNumber = prNode.path("number").asInt();
            task.setPrNumber(prNumber);
            
            // 提取最新提交ID
            String commitId = prNode.path("head").path("sha").asText();
            task.setCommitId(commitId);
            
            return task;
        } catch (Exception e) {
            logger.error("Error parsing Gitee webhook payload: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析GitHub webhook payload
     * @param rootNode JSON根节点
     * @return ReviewTask对象
     */
    private ReviewTask parseGithubWebhookPayload(JsonNode rootNode) {
        try {
            JsonNode prNode = rootNode.path("pull_request");
            JsonNode repoNode = rootNode.path("repository");
            
            ReviewTask task = new ReviewTask();
            task.setPlatform("github");
            
            // 提取仓库信息
            String repoFullName = repoNode.path("full_name").asText();
            task.setRepo(repoFullName);
            
            // 提取PR编号
            int prNumber = prNode.path("number").asInt();
            task.setPrNumber(prNumber);
            
            // 提取最新提交ID
            String commitId = prNode.path("head").path("sha").asText();
            task.setCommitId(commitId);
            
            return task;
        } catch (Exception e) {
            logger.error("Error parsing GitHub webhook payload: {}", e.getMessage());
            return null;
        }
    }
}