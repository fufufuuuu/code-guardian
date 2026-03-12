package src.main.java.com.codeGuardian.diff;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public class DiffParser {
    private final ObjectMapper objectMapper;

    public DiffParser() {
        this.objectMapper = new ObjectMapper();
    }

    public String parse(String diffJson) throws Exception {
        // 解析 Gitee API 返回的 diff JSON
        List<Map<String, Object>> files = objectMapper.readValue(diffJson, List.class);
        
        StringBuilder parsedDiff = new StringBuilder();
        
        for (Map<String, Object> file : files) {
            String filename = (String) file.get("filename");
            String patch = (String) file.get("patch");
            
            parsedDiff.append("File: ").append(filename).append("\n");
            parsedDiff.append(patch).append("\n\n");
        }
        
        return parsedDiff.toString();
    }

    // 可选：添加其他 diff 解析方法
    public String parseRawDiff(String rawDiff) {
        // 解析原始 diff 格式
        // 这里可以根据需要实现更复杂的 diff 解析逻辑
        return rawDiff;
    }
}