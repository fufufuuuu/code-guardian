package com.codeguardian.domain.review;

import java.util.Map;

/**
 * 评分配置
 * 用于映射 scoring.json 配置文件
 */
public class ScoreConfig {
    private Map<String, Integer> severityScore;
    private Map<String, Integer> locationScore;
    private Map<String, Integer> ruleWeight;
    private Limits limits;

    public Map<String, Integer> getSeverityScore() {
        return severityScore;
    }

    public void setSeverityScore(Map<String, Integer> severityScore) {
        this.severityScore = severityScore;
    }

    public Map<String, Integer> getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(Map<String, Integer> locationScore) {
        this.locationScore = locationScore;
    }

    public Map<String, Integer> getRuleWeight() {
        return ruleWeight;
    }

    public void setRuleWeight(Map<String, Integer> ruleWeight) {
        this.ruleWeight = ruleWeight;
    }

    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits;
    }
}
