package com.codeguardian.domain.rule;

import com.codeguardian.domain.model.CodeChunk;
import com.codeguardian.domain.model.Issue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则引擎
 * 负责规则匹配
 */
@Service
public class RuleEngine {
    private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);
    private List<Rule> rules;
    
    public RuleEngine() {
        initRules();
    }
    
    /**
     * 初始化规则
     */
    private void initRules() {
        rules = new ArrayList<>();
        
        try {
            // 从文件加载规则
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("javarole.json");
            
            if (inputStream != null) {
                RuleConfig ruleConfig = objectMapper.readValue(inputStream, RuleConfig.class);
                rules = ruleConfig.getRules();
                logger.info("Loaded {} rules from javarole.json", rules.size());
            } else {
                logger.error("Failed to load rules file: javarole.json");
            }
        } catch (IOException e) {
            logger.error("Error loading rules: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 规则配置类
     */
    private static class RuleConfig {
        private List<Rule> rules;
        
        public List<Rule> getRules() {
            return rules;
        }
        
        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }
    }
    
    /**
     * 检查CodeChunk中的问题
     * @param chunk CodeChunk
     * @return 问题列表
     */
    public List<Issue> check(CodeChunk chunk) {
        List<Issue> issues = new ArrayList<>();
        String code = chunk.getCode();
        String filePath = chunk.getFilePath();
        int startLine = chunk.getStartLine();
        
        if (code == null || code.isEmpty()) {
            return issues;
        }
        
        for (Rule rule : rules) {
            Pattern pattern = Pattern.compile(rule.getPattern());
            Matcher matcher = pattern.matcher(code);
            
            while (matcher.find()) {
                // 计算行号
                int lineNumber = startLine + countLines(code.substring(0, matcher.start()));
                
                Issue issue = new Issue(
                    filePath,
                    lineNumber,
                    rule.getSeverity(),
                    rule.getName(),
                    "请检查并修复此问题"
                );
                issues.add(issue);
            }
        }
        
        return issues;
    }
    
    /**
     * 计算字符串中的行数
     * @param text 文本
     * @return 行数
     */
    private int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\n").length;
    }
    
    /**
     * 获取规则列表
     * @return 规则列表
     */
    public List<Rule> getRules() {
        return rules;
    }
    
    /**
     * 设置规则列表
     * @param rules 规则列表
     */
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}