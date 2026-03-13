package com.codeguardian.domain.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * 评分配置加载器
 * 加载 scoring.json 配置文件并转换为 ScoreConfig 对象
 */
@Component
public class ScoreConfigLoader {
    private static final String CONFIG_PATH = "config/scoring.json";
    private final ObjectMapper objectMapper;

    public ScoreConfigLoader() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 加载评分配置
     * @return ScoreConfig 对象
     */
    public ScoreConfig load() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH)) {
            if (inputStream == null) {
                throw new RuntimeException("Scoring configuration file not found: " + CONFIG_PATH);
            }
            return objectMapper.readValue(inputStream, ScoreConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scoring configuration", e);
        }
    }
}
