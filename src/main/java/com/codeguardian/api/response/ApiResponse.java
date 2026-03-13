package com.codeguardian.api.response;

/**
 * 通用API响应类
 * 用于统一API返回格式
 */
public class ApiResponse {
    private String status;
    private String message;
    private Object data;
    
    private ApiResponse(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 创建成功响应
     * @param message 成功消息
     * @return ApiResponse
     */
    public static ApiResponse success(String message) {
        return new ApiResponse("success", message, null);
    }
    
    /**
     * 创建成功响应（带数据）
     * @param message 成功消息
     * @param data 响应数据
     * @return ApiResponse
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse("success", message, data);
    }
    
    /**
     * 创建错误响应
     * @param message 错误消息
     * @return ApiResponse
     */
    public static ApiResponse error(String message) {
        return new ApiResponse("error", message, null);
    }
    
    /**
     * 创建错误响应（带数据）
     * @param message 错误消息
     * @param data 响应数据
     * @return ApiResponse
     */
    public static ApiResponse error(String message, Object data) {
        return new ApiResponse("error", message, data);
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
}