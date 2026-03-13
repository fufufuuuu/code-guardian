package com.codeguardian.domain.review;

import com.codeguardian.domain.model.Issue;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问题聚合器
 * 负责聚合、去重和过滤问题
 */
@Service
public class IssueAggregator {
    private final IssueScorer issueScorer;
    private final ScoreConfig config;

    public IssueAggregator(IssueScorer issueScorer, ScoreConfigLoader scoreConfigLoader) {
        this.issueScorer = issueScorer;
        this.config = scoreConfigLoader.load();
    }
    
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
        
        // 对每个问题进行评分
        for (Issue issue : uniqueIssues) {
            int score = issueScorer.score(issue);
            issue.setScore(score);
        }
        
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
        
        // 对每个文件的问题按得分排序并限制数量
        List<Issue> result = new ArrayList<>();
        for (List<Issue> fileIssues : issuesByFile.values()) {
            // 按得分降序排序
            fileIssues.sort((a, b) -> {
                Integer scoreA = a.getScore() != null ? a.getScore() : 0;
                Integer scoreB = b.getScore() != null ? b.getScore() : 0;
                return scoreB - scoreA;
            });
            
            // 限制每文件最多问题数
            int count = Math.min(fileIssues.size(), config.getLimits().getMaxIssuesPerFile());
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
        // 按得分降序排序
        issues.sort((a, b) -> {
            Integer scoreA = a.getScore() != null ? a.getScore() : 0;
            Integer scoreB = b.getScore() != null ? b.getScore() : 0;
            return scoreB - scoreA;
        });
        
        // 限制总问题数
        int count = Math.min(issues.size(), config.getLimits().getMaxIssuesPerPR());
        return issues.subList(0, count);
    }
}