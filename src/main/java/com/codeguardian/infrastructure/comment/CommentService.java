package com.codeguardian.infrastructure.comment;

import com.codeguardian.domain.model.Issue;
import com.codeguardian.infrastructure.git.GitClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务
 * 负责发布PR评论
 */
@Service
public class CommentService {
    private GitClient gitClient;
    
    public CommentService() {
    }
    
    /**
     * 设置Git客户端
     * @param gitClient Git客户端
     */
    public void setGitClient(GitClient gitClient) {
        this.gitClient = gitClient;
    }
    
    /**
     * 发布PR评论
     * @param repo 仓库名
     * @param pr PR编号
     * @param issues 问题列表
     * @throws Exception 异常
     */
    public void publish(String repo, Integer pr, List<Issue> issues) throws Exception {
        String comment = generateComment(issues);
        gitClient.comment(repo, pr, comment);
    }
    
    /**
     * 生成评论内容
     * @param issues 问题列表
     * @return 评论内容
     */
    private String generateComment(List<Issue> issues) {
        StringBuilder comment = new StringBuilder();
        
        comment.append("## AI Code Review\n\n");
        
        if (issues.isEmpty()) {
            comment.append("### ✅ 未发现严重问题\n\n");
            comment.append("代码质量良好，未发现明显的安全风险或性能问题。\n");
        } else {
            comment.append("### ⚠️ 发现 " + issues.size() + " 个问题\n\n");
            
            // 按文件分组
            Map<String, List<Issue>> issuesByFile = issues.stream()
                .collect(Collectors.groupingBy(Issue::getFile));
            
            for (Map.Entry<String, List<Issue>> entry : issuesByFile.entrySet()) {
                String file = entry.getKey();
                List<Issue> fileIssues = entry.getValue();
                
                comment.append("#### " + file + "\n\n");
                for (Issue issue : fileIssues) {
                    comment.append("- [" + getSeverityEmoji(issue.getSeverity()) + "] " + issue.getMessage() + "\n");
                    if (issue.getLine() != null) {
                        comment.append("  **行号:** " + issue.getLine() + "\n");
                    }
                    if (issue.getSuggestion() != null && !issue.getSuggestion().isEmpty()) {
                        comment.append("  **建议:** " + issue.getSuggestion() + "\n");
                    }
                    comment.append("\n");
                }
            }
        }
        
        comment.append("---\n\n");
        comment.append("*由 Code Guardian 自动生成*\n");
        
        return comment.toString();
    }
    
    /**
     * 获取严重程度对应的emoji
     * @param severity 严重程度
     * @return emoji
     */
    private String getSeverityEmoji(String severity) {
        switch (severity.toLowerCase()) {
            case "high":
                return "🔴";
            case "medium":
                return "🟡";
            case "low":
                return "🔵";
            default:
                return "⚪";
        }
    }
}