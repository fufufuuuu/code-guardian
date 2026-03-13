package com.codeguardian.infrastructure.ai;

import com.codeguardian.domain.model.CodeChunk;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * 提示构建器
 * 构建AI审查的提示
 * @author renlong.fu
 */
@Component
public class PromptBuilder {
    private String template;
    
    public PromptBuilder() {
        loadTemplate();
    }
    
    /**
     * 加载提示模板
     */
    private void loadTemplate() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("prompt/java-review.prompt")) {
            Scanner scanner = null;
            if (inputStream != null) {
                scanner = new Scanner(inputStream, "UTF-8");
            }
            if (scanner != null) {
                template = scanner.useDelimiter("\\A").next();
                scanner.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 构建代码审查提示词
     * @param chunk CodeChunk
     * @return 提示词
     */
    public String buildPrompt(CodeChunk chunk) {
        return template
                .replace("{fileName}", chunk.getFilePath())
                .replace("{code}", chunk.getCode());
    }
}