package com.codeguardian.infrastructure.ai;

import com.codeguardian.config.AppConfig;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DeepSeek客户端实现
 */
@Service
public class DeepSeekClient implements LLMService {
    private final String API_URL;
    private final String API_KEY;
    private final ObjectMapper objectMapper;
    
    public DeepSeekClient(AppConfig appConfig) {
        this.API_URL = appConfig.getDeepseekApiUrl();
        this.API_KEY = appConfig.getDeepseekApiKey();
        this.objectMapper = new ObjectMapper();
    }
    
    public DeepSeekClient() {
        this.API_URL = "https://api.deepseek.com/v1/chat/completions";
        this.API_KEY = "your_api_key";
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String review(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-coder-v1.5");
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 2048);

        // 构建消息
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        requestBody.put("messages", new Object[] { message });

        // 发送请求
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = objectMapper.writeValueAsString(requestBody).getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 读取响应
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);
        Map<String, Object> choice = ((Map<String, Object>) ((java.util.List<?>) responseMap.get("choices")).get(0));
        Map<String, String> messageResponse = (Map<String, String>) choice.get("message");

        return messageResponse.get("content");
    }
}