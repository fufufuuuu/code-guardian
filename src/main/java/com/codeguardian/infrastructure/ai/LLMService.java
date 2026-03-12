package com.codeguardian.infrastructure.ai;

/**
 * LLM服务接口
 * 定义AI审查的统一接口
 */
public interface LLMService {
    
    /**
     * 审查代码
     * @param prompt 提示词
     * @return AI审查结果
     * @throws Exception 异常
     */
    String review(String prompt) throws Exception;
}