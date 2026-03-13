package com.codeguardian.infrastructure.git;

import com.codeguardian.config.AppConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * GitHub客户端实现
 */
public class GithubClient implements GitClient {
    private static final String API_BASE_URL = "https://api.github.com";
    private final String ACCESS_TOKEN;
    private final ObjectMapper objectMapper;
    
    public GithubClient(AppConfig appConfig) {
        this.ACCESS_TOKEN = appConfig.getGiteeApiAccessToken(); // 暂时使用相同的token配置
        this.objectMapper = new ObjectMapper();
    }
    
    public GithubClient() {
        this.ACCESS_TOKEN = "your_access_token";
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<PRFile> getPullRequestFiles(String repo, Integer pr) throws Exception {
        String apiUrl = API_BASE_URL + "/repos/" + repo + "/pulls/" + pr + "/files";
        String response = sendGetRequest(apiUrl);
        
        List<Map<String, Object>> files = objectMapper.readValue(response, List.class);
        List<PRFile> prFiles = new ArrayList<>();
        
        for (Map<String, Object> file : files) {
            String fileName = (String) file.get("filename");
            String patch = (String) file.get("patch");
            prFiles.add(new PRFile(fileName, patch));
        }
        
        return prFiles;
    }

    @Override
    public void comment(String repo, Integer pr, String body) throws Exception {
        String apiUrl = API_BASE_URL + "/repos/" + repo + "/issues/" + pr + "/comments";
        String jsonInputString = "{\"body\": \"" + body + "\"}";
        sendPostRequest(apiUrl, jsonInputString);
    }
    
    private String sendGetRequest(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        
        return readResponse(connection);
    }
    
    private String sendPostRequest(String apiUrl, String requestBody) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setDoOutput(true);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        return readResponse(connection);
    }
    
    private String readResponse(HttpURLConnection connection) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        return response.toString();
    }
}