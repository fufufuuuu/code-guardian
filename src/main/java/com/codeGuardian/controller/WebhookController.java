package src.main.java.com.codeGuardian.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.service.ReviewService;

@RestController
public class WebhookController {
    private final ReviewService reviewService;

    public WebhookController() {
        this.reviewService = new ReviewService();
    }

    @PostMapping("/webhook")
    public String handleWebhook(@RequestBody String payload) {
        try {
            System.out.println("Received webhook payload: " + payload);
            
            // 处理 webhook 事件
            reviewService.processWebhook(payload);
            
            return "{\"status\": \"success\", \"message\": \"Webhook processed successfully\"}";
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            return "{\"status\": \"error\", \"message\": \"Failed to process webhook\"}";
        }
    }
}