package com.codeguardian.domain.review;

/**
 * 限制配置
 * 用于映射 scoring.json 中的 limits 部分
 */
public class Limits {
    private Integer maxIssuesPerFile;
    private Integer maxIssuesPerPR;

    public Integer getMaxIssuesPerFile() {
        return maxIssuesPerFile;
    }

    public void setMaxIssuesPerFile(Integer maxIssuesPerFile) {
        this.maxIssuesPerFile = maxIssuesPerFile;
    }

    public Integer getMaxIssuesPerPR() {
        return maxIssuesPerPR;
    }

    public void setMaxIssuesPerPR(Integer maxIssuesPerPR) {
        this.maxIssuesPerPR = maxIssuesPerPR;
    }
}
