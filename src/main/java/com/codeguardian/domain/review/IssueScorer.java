package com.codeguardian.domain.review;

import com.codeguardian.domain.model.Issue;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Issue 评分器
 * 根据评分规则计算 Issue 的得分
 */
@Component
public class IssueScorer {
    private final ScoreConfig config;

    public IssueScorer(ScoreConfigLoader scoreConfigLoader) {
        this.config = scoreConfigLoader.load();
    }

    /**
     * 计算 Issue 的得分
     * @param issue Issue 对象
     * @return 得分
     */
    public int score(Issue issue) {
        int severityScore = getSeverityScore(issue.getSeverity());
        int ruleWeight = getRuleWeight(issue.getRuleId());
        int locationScore = getLocationScore(issue.getFile());
        
        return severityScore + ruleWeight + locationScore;
    }

    /**
     * 获取严重程度得分
     * @param severity 严重程度
     * @return 得分
     */
    private int getSeverityScore(String severity) {
        Map<String, Integer> severityScores = config.getSeverityScore();
        return severityScores.getOrDefault(severity, 0);
    }

    /**
     * 获取规则权重得分
     * @param ruleId 规则 ID
     * @return 得分
     */
    private int getRuleWeight(String ruleId) {
        Map<String, Integer> ruleWeights = config.getRuleWeight();
        return ruleWeights.getOrDefault(ruleId, 0);
    }

    /**
     * 获取位置得分
     * @param file 文件路径
     * @return 得分
     */
    private int getLocationScore(String file) {
        Map<String, Integer> locationScores = config.getLocationScore();
        for (Map.Entry<String, Integer> entry : locationScores.entrySet()) {
            if (file.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return 0;
    }
}
