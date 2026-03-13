package com.codeguardian.domain.model;

/**
 * 问题模型
 * 表示代码审查中发现的问题
 */
public class Issue {
    private String ruleId;
    private String file;
    private Integer line;
    private String severity;
    private String message;
    private String suggestion;
    private Integer score;
    
    public Issue() {
    }
    
    public Issue(String ruleId, String file, Integer line, String severity, String message, String suggestion) {
        this.ruleId = ruleId;
        this.file = file;
        this.line = line;
        this.severity = severity;
        this.message = message;
        this.suggestion = suggestion;
    }
    
    public Issue(String file, Integer line, String severity, String message, String suggestion) {
        this.file = file;
        this.line = line;
        this.severity = severity;
        this.message = message;
        this.suggestion = suggestion;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
    
    @Override
    public String toString() {
        return "Issue{" +
                "ruleId='" + ruleId + '\'' +
                ", file='" + file + '\'' +
                ", line=" + line +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                ", score=" + score +
                '}';
    }
}