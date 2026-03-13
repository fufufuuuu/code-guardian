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
 * Gitee客户端实现
 */
public class GiteeClient implements GitClient {
    private final String API_BASE_URL;
    private final String ACCESS_TOKEN;
    private final ObjectMapper objectMapper;
    
    public GiteeClient(AppConfig appConfig) {
        this.API_BASE_URL = appConfig.getGiteeApiBaseUrl();
        this.ACCESS_TOKEN = appConfig.getGiteeApiAccessToken();
        this.objectMapper = new ObjectMapper();
    }
    
    public GiteeClient() {
        this.API_BASE_URL = "https://gitee.com/api/v5";
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
        String apiUrl = API_BASE_URL + "/repos/" + repo + "/pulls/" + pr + "/comments";
        String jsonInputString = "{\"body\": \"" + body + "\"}";
        sendPostRequest(apiUrl, jsonInputString);
    }
    
    private String sendGetRequest(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + ACCESS_TOKEN);
        
        return readResponse(connection);
    }
    
    private String sendPostRequest(String apiUrl, String requestBody) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "token " + ACCESS_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json");
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