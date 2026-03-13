package com.codeguardian.infrastructure.git;

/**
 * PR文件模型
 * 表示PR中的文件
 */
public class PRFile {
    private String fileName;
    private String patch;
    
    public PRFile() {
    }
    
    public PRFile(String fileName, String patch) {
        this.fileName = fileName;
        this.patch = patch;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }
    
    @Override
    public String toString() {
        return "PRFile{" +
                "fileName='" + fileName + '\'' +
                ", patch.length()=" + (patch != null ? patch.length() : 0) +
                '}';
    }
}