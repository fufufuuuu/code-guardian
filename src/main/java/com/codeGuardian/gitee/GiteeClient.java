package src.main.java.com.codeGuardian.gitee;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GiteeClient {
    private static final String API_BASE_URL = "https://gitee.com/api/v5";
    private static final String ACCESS_TOKEN = "your_access_token";

    public String getPRDiff(String prUrl) throws Exception {
        // 从 PR URL 中提取仓库和 PR 号
        // 这里需要根据实际的 URL 格式进行解析
        String repo = "example/repo";
        int prNumber = 1;
        
        String apiUrl = API_BASE_URL + "/repos/" + repo + "/pulls/" + prNumber + "/files";
        
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + ACCESS_TOKEN);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        return response.toString();
    }

    public void commentPR(String prUrl, String comment) throws Exception {
        // 从 PR URL 中提取仓库和 PR 号
        // 这里需要根据实际的 URL 格式进行解析
        String repo = "example/repo";
        int prNumber = 1;
        
        String apiUrl = API_BASE_URL + "/repos/" + repo + "/pulls/" + prNumber + "/comments";
        
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "token " + ACCESS_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        String jsonInputString = "{\"body\": \"" + comment + "\"}";
        
        try (var os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        System.out.println("Comment posted successfully: " + response.toString());
    }
}