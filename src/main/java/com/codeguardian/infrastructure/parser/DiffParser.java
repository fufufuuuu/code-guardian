package com.codeguardian.infrastructure.parser;

import com.codeguardian.domain.model.CodeChunk;
import com.codeguardian.infrastructure.git.PRFile;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diff解析器
 * 解析PR文件并生成CodeChunk列表
 * @author renlong.fu
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
            if (needReview(file.getFileName())) {
                chunks.addAll(parsePatch(file));
            }
        }
        
        return chunks;
    }
    
    /**
     * 解析patch并生成CodeChunk列表
     * @param file PR文件
     * @return CodeChunk列表
     */
    private List<CodeChunk> parsePatch(PRFile file) {
        List<CodeChunk> chunks = new ArrayList<>();
        String patch = file.getPatch();
        
        if (patch == null || patch.isEmpty()) {
            return chunks;
        }
        
        String[] lines = patch.split("\n");
        int newLineNumber = 0;
        StringBuilder code = new StringBuilder();
        
        for (String line : lines) {
            if (line.startsWith("@@")) {
                // 解析新代码的起始行号
                newLineNumber = extractNewLine(line);
                // 如果已经有代码，生成一个CodeChunk
                if (code.length() > 0) {
                    CodeChunk chunk = createCodeChunk(file.getFileName(), newLineNumber - countLines(code.toString()), code.toString());
                    chunks.add(chunk);
                    code = new StringBuilder();
                }
            } else if (line.startsWith("+") && !line.startsWith("+++")) {
                // 提取新增代码
                code.append(line.substring(1)).append("\n");
            }
        }
        
        // 处理最后一个chunk
        if (code.length() > 0) {
            CodeChunk chunk = createCodeChunk(file.getFileName(), newLineNumber, code.toString());
            chunks.add(chunk);
        }
        
        return chunks;
    }
    
    /**
     * 从hunk header中提取新代码的起始行号
     * @param header hunk header
     * @return 新代码的起始行号
     */
    private int extractNewLine(String header) {
        Pattern p = Pattern.compile("\\+(\\d+)");
        Matcher m = p.matcher(header);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
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
     * 创建CodeChunk
     * @param filePath 文件路径
     * @param startLine 起始行号
     * @param code 代码
     * @return CodeChunk
     */
    private CodeChunk createCodeChunk(String filePath, int startLine, String code) {
        String language = detectLanguage(filePath);
        return new CodeChunk(filePath, startLine, language, code);
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
    
    /**
     * 判断文件是否需要审查
     * @param fileName 文件名
     * @return 是否需要审查
     */
    private boolean needReview(String fileName) {
        // 过滤不需要审查的文件类型
        if (fileName.endsWith(".md") || fileName.endsWith(".json") || fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return false;
        }
        // 过滤测试文件
        if (fileName.contains("test") || fileName.contains("Test")) {
            return false;
        }
        // 只审查代码文件
        return fileName.endsWith(".java") || fileName.endsWith(".kt") || fileName.endsWith(".py") || 
               fileName.endsWith(".go") || fileName.endsWith(".js") || fileName.endsWith(".ts");
    }
}