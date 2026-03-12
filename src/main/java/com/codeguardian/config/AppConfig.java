package com.codeguardian.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置类
 * 读取application.properties中的配置
 */
@Configuration
public class AppConfig {
    
    // 服务器配置
    @Value("${server.port:8080}")
    private int serverPort;
    
    // Gitee API配置
    @Value("${gitee.api.base-url:https://gitee.com/api/v5}")
    private String giteeApiBaseUrl;
    
    @Value("${gitee.api.access-token:your_access_token}")
    private String giteeApiAccessToken;
    
    // Deepseek API配置
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;
    
    @Value("${deepseek.api.key:your_api_key}")
    private String deepseekApiKey;
    
    // 规则引擎配置
    @Value("${rule.engine.enabled:true}")
    private boolean ruleEngineEnabled;
    
    // 代码块分割配置
    @Value("${chunk.max-size:4000}")
    private int chunkMaxSize;
    
    @Value("${chunk.overlap-size:200}")
    private int chunkOverlapSize;
    
    // 问题过滤配置
    @Value("${issue.max-per-file:5}")
    private int issueMaxPerFile;
    
    @Value("${issue.max-total:20}")
    private int issueMaxTotal;

    public int getServerPort() {
        return serverPort;
    }

    public String getGiteeApiBaseUrl() {
        return giteeApiBaseUrl;
    }

    public String getGiteeApiAccessToken() {
        return giteeApiAccessToken;
    }

    public String getDeepseekApiUrl() {
        return deepseekApiUrl;
    }

    public String getDeepseekApiKey() {
        return deepseekApiKey;
    }

    public boolean isRuleEngineEnabled() {
        return ruleEngineEnabled;
    }

    public int getChunkMaxSize() {
        return chunkMaxSize;
    }

    public int getChunkOverlapSize() {
        return chunkOverlapSize;
    }

    public int getIssueMaxPerFile() {
        return issueMaxPerFile;
    }

    public int getIssueMaxTotal() {
        return issueMaxTotal;
    }
}