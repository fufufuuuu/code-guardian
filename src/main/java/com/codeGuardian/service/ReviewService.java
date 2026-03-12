package src.main.java.com.codeGuardian.service;

import com.example.gitee.GiteeClient;
import com.example.diff.DiffParser;
import com.example.llm.DeepseekClient;

public class ReviewService {
    private final GiteeClient giteeClient;
    private final DiffParser diffParser;
    private final DeepseekClient deepseekClient;

    public ReviewService() {
        this.giteeClient = new GiteeClient();
        this.diffParser = new DiffParser();
        this.deepseekClient = new DeepseekClient();
    }

    public void processWebhook(String payload) {
        try {
            // 解析 webhook 事件
            // 这里需要根据实际的 webhook 格式进行解析
            System.out.println("Processing webhook payload...");
            
            // 获取 PR 信息
            String prUrl = "https://gitee.com/example/repo/pulls/1";
            
            // 获取 PR diff
            String diff = giteeClient.getPRDiff(prUrl);
            
            // 解析 diff
            String parsedDiff = diffParser.parse(diff);
            
            // 使用 LLM 进行代码审查
            String review = deepseekClient.reviewCode(parsedDiff);
            
            // 将审查结果评论到 PR
            giteeClient.commentPR(prUrl, review);
            
            System.out.println("Webhook processed successfully");
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }
}