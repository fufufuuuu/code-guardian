package com.codeguardian.infrastructure.ai;

import com.codeguardian.domain.model.Issue;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 问题提取器
 * 从AI响应中提取问题
 */
@Component
public class IssueExtractor {
    
    /**
     * 从AI响应中提取Issue列表
     * @param aiResponse AI响应
     * @return Issue列表
     */
    public List<Issue> extract(String aiResponse) {
        List<Issue> issues = new ArrayList<>();
        
        if (aiResponse == null || aiResponse.isEmpty()) {
            return issues;
        }
        
        // 简单的解析逻辑，实际应该根据AI返回的格式进行调整
        String[] lines = aiResponse.split("\n");
        Issue currentIssue = null;
        StringBuilder description = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            
            // 检测问题开始
            if (line.startsWith("- 问题描述:")) {
                // 保存之前的问题
                if (currentIssue != null) {
                    currentIssue.setMessage(description.toString());
                    issues.add(currentIssue);
                }
                
                // 创建新问题
                currentIssue = new Issue();
                description = new StringBuilder();
                description.append(line.substring("- 问题描述:".length()).trim());
            } else if (line.startsWith("- 严重程度:")) {
                if (currentIssue != null) {
                    String severity = line.substring("- 严重程度:".length()).trim();
                    currentIssue.setSeverity(severity);
                }
            } else if (line.startsWith("- 改进建议:")) {
                if (currentIssue != null) {
                    String suggestion = line.substring("- 改进建议:".length()).trim();
                    currentIssue.setSuggestion(suggestion);
                }
            } else if (line.startsWith("- 相关代码片段:")) {
                // 跳过代码片段
            } else if (currentIssue != null) {
                // 累积描述
                description.append("\n").append(line);
            }
        }
        
        // 保存最后一个问题
        if (currentIssue != null) {
            currentIssue.setMessage(description.toString());
            issues.add(currentIssue);
        }
        
        return issues;
    }
}