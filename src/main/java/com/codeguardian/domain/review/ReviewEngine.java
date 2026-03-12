package com.codeguardian.domain.review;

import com.codeguardian.domain.model.CodeChunk;
import com.codeguardian.domain.model.Issue;
import com.codeguardian.domain.rule.RuleEngine;
import com.codeguardian.infrastructure.ai.LLMService;
import com.codeguardian.infrastructure.ai.PromptBuilder;
import com.codeguardian.infrastructure.ai.IssueExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 审查引擎
 * 执行规则审查和AI审查
 */
@Service
public class ReviewEngine {
    private static final Logger logger = LoggerFactory.getLogger(ReviewEngine.class);
    @Autowired
    private RuleEngine ruleEngine;
    @Autowired
    private LLMService llmService;
    @Autowired
    private PromptBuilder promptBuilder;
    @Autowired
    private IssueExtractor issueExtractor;
    @Autowired
    private IssueAggregator issueAggregator;
    
    public ReviewEngine() {
    }
    
    /**
     * 执行代码审查
     * @param chunk CodeChunk
     * @return 审查结果（Issue列表）
     * @throws Exception 异常
     */
    public List<Issue> review(CodeChunk chunk) throws Exception {
        // 规则审查
        List<Issue> ruleIssues = ruleEngine.check(chunk);
        
        // AI审查
        List<Issue> aiIssues = new ArrayList<>();
        try {
            String prompt = promptBuilder.buildPrompt(chunk);
            String aiResponse = llmService.review(prompt);
            aiIssues = issueExtractor.extract(aiResponse);
            
            // 设置文件路径和行号
            for (Issue issue : aiIssues) {
                issue.setFile(chunk.getFilePath());
                issue.setLine(chunk.getStartLine());
            }
        } catch (Exception e) {
            // 记录错误但不影响整体流程
            logger.warn("AI review failed for file {}: {}", chunk.getFilePath(), e.getMessage());
        }
        
        // 聚合问题
        return issueAggregator.aggregate(ruleIssues, aiIssues);
    }
    
    /**
     * 批量执行代码审查
     * @param chunks CodeChunk列表
     * @return 审查结果（Issue列表）
     * @throws Exception 异常
     */
    public List<Issue> reviewBatch(List<CodeChunk> chunks) throws Exception {
        List<Issue> allIssues = new ArrayList<>();
        
        for (CodeChunk chunk : chunks) {
            List<Issue> issues = review(chunk);
            allIssues.addAll(issues);
        }
        
        // 对所有问题进行最终聚合
        return issueAggregator.aggregate(allIssues);
    }
}