package com.codeguardian.infrastructure.ai;

import com.codeguardian.domain.model.CodeChunk;
import org.springframework.stereotype.Component;

/**
 * 提示构建器
 * 构建AI审查的提示
 */
@Component
public class PromptBuilder {
    
    /**
     * 构建代码审查提示词
     * @param chunk CodeChunk
     * @return 提示词
     */
    public String buildPrompt(CodeChunk chunk) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的代码审查工具，负责分析代码变更并提供详细的审查报告。\n\n");
        prompt.append("请对以下代码进行审查，指出潜在的问题和改进建议：\n\n");
        prompt.append("文件路径: " + chunk.getFilePath() + "\n");
        prompt.append("起始行号: " + chunk.getStartLine() + "\n");
        prompt.append("编程语言: " + chunk.getLanguage() + "\n\n");
        prompt.append("代码内容:\n");
        prompt.append("```" + chunk.getLanguage() + "\n");
        prompt.append(chunk.getCode());
        prompt.append("```\n\n");
        prompt.append("请关注以下几个方面：\n");
        prompt.append("1. 代码质量和可读性\n");
        prompt.append("2. 潜在的安全漏洞\n");
        prompt.append("3. 性能优化建议\n");
        prompt.append("4. 最佳实践遵循情况\n");
        prompt.append("5. 潜在的bug\n\n");
        prompt.append("请提供结构化的审查报告，包括：\n");
        prompt.append("- 问题描述\n");
        prompt.append("- 严重程度（高/中/低）\n");
        prompt.append("- 改进建议\n");
        prompt.append("- 相关代码片段\n\n");
        prompt.append("审查报告应该清晰、详细，并且易于理解。");
        
        return prompt.toString();
    }
}