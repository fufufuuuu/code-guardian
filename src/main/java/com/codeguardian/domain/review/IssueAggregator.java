package com.codeguardian.domain.review;

import com.codeguardian.domain.model.Issue;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 问题聚合器
 * 负责聚合、去重和过滤问题
 */
@Service
public class IssueAggregator {
    private static final int MAX_ISSUES_PER_FILE = 2;
    private static final int MAX_TOTAL_ISSUES = 10;
    
    /**
     * 聚合规则审查和AI审查的问题
     * @param ruleIssues 规则审查问题
     * @param aiIssues AI审查问题
     * @return 聚合后的问题列表
     */
    public List<Issue> aggregate(List<Issue> ruleIssues, List<Issue> aiIssues) {
        List<Issue> allIssues = new ArrayList<>();
        allIssues.addAll(ruleIssues);
        allIssues.addAll(aiIssues);
        return aggregate(allIssues);
    }
    
    /**
     * 聚合问题列表
     * @param issues 问题列表
     * @return 聚合后的问题列表
     */
    public List<Issue> aggregate(List<Issue> issues) {
        // 去重
        List<Issue> uniqueIssues = deduplicate(issues);
        
        // 按文件分组并限制每文件问题数
        List<Issue> limitedByFile = limitByFile(uniqueIssues);
        
        // 限制总问题数
        return limitTotal(limitedByFile);
    }
    
    /**
     * 去重问题
     * @param issues 问题列表
     * @return 去重后的问题列表
     */
    private List<Issue> deduplicate(List<Issue> issues) {
        Map<String, Issue> uniqueMap = new HashMap<>();
        
        for (Issue issue : issues) {
            String key = issue.getFile() + ":" + issue.getLine() + ":" + issue.getMessage();
            if (!uniqueMap.containsKey(key)) {
                uniqueMap.put(key, issue);
            }
        }
        
        return new ArrayList<>(uniqueMap.values());
    }
    
    /**
     * 按文件限制问题数
     * @param issues 问题列表
     * @return 按文件限制后的问题列表
     */
    private List<Issue> limitByFile(List<Issue> issues) {
        Map<String, List<Issue>> issuesByFile = new HashMap<>();
        
        // 按文件分组
        for (Issue issue : issues) {
            String file = issue.getFile();
            if (!issuesByFile.containsKey(file)) {
                issuesByFile.put(file, new ArrayList<>());
            }
            issuesByFile.get(file).add(issue);
        }
        
        // 对每个文件的问题按严重程度排序并限制数量
        List<Issue> result = new ArrayList<>();
        for (List<Issue> fileIssues : issuesByFile.values()) {
            // 按严重程度排序（高 > 中 > 低）
            fileIssues.sort((a, b) -> {
                int severityA = getSeverityScore(a.getSeverity());
                int severityB = getSeverityScore(b.getSeverity());
                return severityB - severityA;
            });
            
            // 限制每文件最多2个问题
            int count = Math.min(fileIssues.size(), MAX_ISSUES_PER_FILE);
            result.addAll(fileIssues.subList(0, count));
        }
        
        return result;
    }
    
    /**
     * 限制总问题数
     * @param issues 问题列表
     * @return 限制后的问题列表
     */
    private List<Issue> limitTotal(List<Issue> issues) {
        // 按严重程度排序
        issues.sort((a, b) -> {
            int severityA = getSeverityScore(a.getSeverity());
            int severityB = getSeverityScore(b.getSeverity());
            return severityB - severityA;
        });
        
        // 限制总问题数最多10个
        int count = Math.min(issues.size(), MAX_TOTAL_ISSUES);
        return issues.subList(0, count);
    }
    
    /**
     * 获取严重程度分数
     * @param severity 严重程度字符串
     * @return 严重程度分数
     */
    private int getSeverityScore(String severity) {
        switch (severity.toLowerCase()) {
            case "high":
                return 3;
            case "medium":
                return 2;
            case "low":
                return 1;
            default:
                return 0;
        }
    }
}