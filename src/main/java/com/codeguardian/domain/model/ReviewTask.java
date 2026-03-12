package com.codeguardian.domain.model;

/**
 * 代码审查任务模型
 */
public class ReviewTask {
    private String platform;
    private String repo;
    private Integer prNumber;
    private String commitId;
    
    public ReviewTask() {
    }
    
    public ReviewTask(String platform, String repo, Integer prNumber, String commitId) {
        this.platform = platform;
        this.repo = repo;
        this.prNumber = prNumber;
        this.commitId = commitId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public Integer getPrNumber() {
        return prNumber;
    }

    public void setPrNumber(Integer prNumber) {
        this.prNumber = prNumber;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
    
    @Override
    public String toString() {
        return "ReviewTask{" +
                "platform='" + platform + '\'' +
                ", repo='" + repo + '\'' +
                ", prNumber=" + prNumber +
                ", commitId='" + commitId + '\'' +
                '}';
    }
}