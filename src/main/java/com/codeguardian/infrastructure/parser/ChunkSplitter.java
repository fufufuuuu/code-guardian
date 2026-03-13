package com.codeguardian.infrastructure.parser;

import com.codeguardian.domain.model.CodeChunk;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码块分割器
 * 将大的代码块分割成小的代码块
 */
@Component
public class ChunkSplitter {
    private static final int MAX_LINES = 200;
    
    /**
     * 分割CodeChunk
     * @param chunk 原始CodeChunk
     * @return 分割后的CodeChunk列表
     */
    public List<CodeChunk> split(CodeChunk chunk) {
        List<CodeChunk> chunks = new ArrayList<>();
        String code = chunk.getCode();
        
        if (code == null || code.isEmpty()) {
            return chunks;
        }
        
        // 尝试按函数切分
        List<CodeChunk> functionChunks = splitByFunction(chunk);
        if (!functionChunks.isEmpty()) {
            chunks.addAll(functionChunks);
        } else {
            // 按行数切分
            chunks.addAll(splitByLines(chunk));
        }
        
        return chunks;
    }
    
    /**
     * 按函数切分
     * @param chunk 原始CodeChunk
     * @return 分割后的CodeChunk列表
     */
    private List<CodeChunk> splitByFunction(CodeChunk chunk) {
        List<CodeChunk> chunks = new ArrayList<>();
        String code = chunk.getCode();
        String language = chunk.getLanguage();
        String filePath = chunk.getFilePath();
        int startLine = chunk.getStartLine();
        
        // 对于Java文件，按方法切分
        if ("java".equals(language)) {
            String[] lines = code.split("\n");
            int lineCount = lines.length;
            int methodStart = 0;
            boolean inMethod = false;
            int braceCount = 0;
            
            for (int i = 0; i < lineCount; i++) {
                String line = lines[i];
                
                // 检测方法开始
                if (!inMethod && (line.contains("public ") || line.contains("private ") || line.contains("protected ")) && line.contains("(")) {
                    methodStart = i;
                    inMethod = true;
                    braceCount = 0;
                }
                
                // 计数大括号
                if (inMethod) {
                    for (char c : line.toCharArray()) {
                        if (c == '{') {
                            braceCount++;
                        } else if (c == '}') {
                            braceCount--;
                        }
                    }
                    
                    // 方法结束
                    if (braceCount == 0) {
                        StringBuilder methodCode = new StringBuilder();
                        for (int j = methodStart; j <= i; j++) {
                            methodCode.append(lines[j] + "\n");
                        }
                        chunks.add(new CodeChunk(filePath, startLine + methodStart + 1, language, methodCode.toString()));
                        inMethod = false;
                    }
                }
            }
        }
        
        return chunks;
    }
    
    /**
     * 按行数切分
     * @param chunk 原始CodeChunk
     * @return 分割后的CodeChunk列表
     */
    private List<CodeChunk> splitByLines(CodeChunk chunk) {
        List<CodeChunk> chunks = new ArrayList<>();
        String code = chunk.getCode();
        String language = chunk.getLanguage();
        String filePath = chunk.getFilePath();
        int startLine = chunk.getStartLine();
        
        String[] lines = code.split("\n");
        int lineCount = lines.length;
        int currentLine = 0;
        
        while (currentLine < lineCount) {
            int endLine = Math.min(currentLine + MAX_LINES, lineCount);
            StringBuilder chunkCode = new StringBuilder();
            
            for (int i = currentLine; i < endLine; i++) {
                chunkCode.append(lines[i] + "\n");
            }
            
            chunks.add(new CodeChunk(filePath, startLine + currentLine + 1, language, chunkCode.toString()));
            currentLine = endLine;
        }
        
        return chunks;
    }
}