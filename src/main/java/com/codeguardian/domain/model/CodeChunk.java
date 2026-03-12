package com.codeguardian.domain.model;

/**
 * 代码块模型
 * 表示一段需要分析的代码
 */
public class CodeChunk {
    private String filePath;
    private Integer startLine;
    private String language;
    private String code;
    
    public CodeChunk() {
    }
    
    public CodeChunk(String filePath, Integer startLine, String language, String code) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.language = language;
        this.code = code;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getStartLine() {
        return startLine;
    }

    public void setStartLine(Integer startLine) {
        this.startLine = startLine;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    @Override
    public String toString() {
        return "CodeChunk{" +
                "filePath='" + filePath + '\'' +
                ", startLine=" + startLine +
                ", language='" + language + '\'' +
                ", code.length()=" + (code != null ? code.length() : 0) +
                '}';
    }
}