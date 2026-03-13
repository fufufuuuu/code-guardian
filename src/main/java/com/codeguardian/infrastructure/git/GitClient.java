package com.codeguardian.infrastructure.git;

import java.util.List;
import com.codeguardian.infrastructure.git.PRFile;

/**
 * Git客户端接口
 * 定义Git操作的统一接口
 */
public interface GitClient {
    
    /**
     * 获取PR文件列表
     * @param repo 仓库名
     * @param pr PR编号
     * @return PR文件列表
     * @throws Exception 异常
     */
    List<PRFile> getPullRequestFiles(String repo, Integer pr) throws Exception;
    
    /**
     * 发布PR评论
     * @param repo 仓库名
     * @param pr PR编号
     * @param body 评论内容
     * @throws Exception 异常
     */
    void comment(String repo, Integer pr, String body) throws Exception;
}