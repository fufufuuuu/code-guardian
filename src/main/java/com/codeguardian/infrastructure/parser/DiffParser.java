package com.codeguardian.infrastructure.parser;

import com.codeguardian.domain.model.CodeChunk;
import com.codeguardian.infrastructure.git.PRFile;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Diff解析器
 * 解析PR文件并生成CodeChunk列表
 */
@Component
public class DiffParser {
    
    /**
     * 解析PR文件列表并生成CodeChunk列表
     * @param files PR文件列表
     * @return CodeChunk列表
     */
    public List<CodeChunk> parse(List<PRFile> files) {
        List<CodeChunk> chunks = new ArrayList<>();
        
        for (PRFile file : files) {
            List<CodeChunk> fileChunks = parseFile(file);
            chunks.addAll(fileChunks);
        }
        
        return chunks;
    }
    
    /**
     * 解析单个PR文件并生成CodeChunk列表
     * @param file PR文件
     * @return CodeChunk列表
     */
    private List<CodeChunk> parseFile(PRFile file) {
        List<CodeChunk> chunks = new ArrayList<>();
        String fileName = file.getFileName();
        String patch = file.getPatch();
        
        if (patch == null || patch.isEmpty()) {
            return chunks;
        }
        
        // 提取新增代码
        String addedCode = extractAddedCode(patch);
        if (!addedCode.isEmpty()) {
            String language = detectLanguage(fileName);
            // 暂时使用1作为起始行号，实际应该从patch中解析
            chunks.add(new CodeChunk(fileName, 1, language, addedCode));
        }
        
        return chunks;
    }
    
    /**
     * 从patch中提取新增代码
     * @param patch patch内容
     * @return 新增代码
     */
    private String extractAddedCode(String patch) {
        StringBuilder addedCode = new StringBuilder();
        String[] lines = patch.split("\n");
        
        for (String line : lines) {
            if (line.startsWith("+")) {
                // 跳过 +++ 行
                if (!line.startsWith("+++")) {
                    addedCode.append(line.substring(1) + "\n");
                }
            }
        }
        
        return addedCode.toString();
    }
    
    /**
     * 检测文件语言
     * @param fileName 文件名
     * @return 语言名称
     */
    private String detectLanguage(String fileName) {
        if (fileName.endsWith(".java")) {
            return "java";
        } else if (fileName.endsWith(".py")) {
            return "python";
        } else if (fileName.endsWith(".go")) {
            return "go";
        } else if (fileName.endsWith(".js") || fileName.endsWith(".ts")) {
            return "javascript";
        } else if (fileName.endsWith(".html")) {
            return "html";
        } else if (fileName.endsWith(".css")) {
            return "css";
        } else {
            return "unknown";
        }
    }
}