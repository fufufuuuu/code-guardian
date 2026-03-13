package com.codeguardian.domain.rule;

/**
 * 规则模型
 * 表示代码审查规则
 */
public class Rule {
    private String id;
    private String name;
    private String pattern;
    private String type;
    private String severity;
    private boolean triggerAI;
    
    public Rule() {
    }
    
    public Rule(String id, String name, String pattern, String type, String severity, boolean triggerAI) {
        this.id = id;
        this.name = name;
        this.pattern = pattern;
        this.type = type;
        this.severity = severity;
        this.triggerAI = triggerAI;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public boolean isTriggerAI() {
        return triggerAI;
    }

    public void setTriggerAI(boolean triggerAI) {
        this.triggerAI = triggerAI;
    }
    
    @Override
    public String toString() {
        return "Rule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", severity='" + severity + '\'' +
                '}';
    }
}