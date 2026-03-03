package com.example.demo.common;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Data
public class ApiResult {
    private Integer code;
    private String message;
    private Object data;
    private String timestamp;
    
    // 错误详情字段
    private List<ErrorDetail> errors;
    
    public ApiResult() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    public ApiResult(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    public ApiResult(Integer code, String message, Object data, List<ErrorDetail> errors) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    // 成功响应
    public static ApiResult success() {
        return new ApiResult(200, "success", null);
    }
    
    public static ApiResult success(Object data) {
        return new ApiResult(200, "success", data);
    }
    
    public static ApiResult success(String message, Object data) {
        return new ApiResult(200, message, data);
    }
    
    // 错误响应
    public static ApiResult error(String message) {
        return new ApiResult(500, message, null);
    }
    
    public static ApiResult error(Integer code, String message) {
        return new ApiResult(code, message, null);
    }
    
    public static ApiResult error(Integer code, String message, List<ErrorDetail> errors) {
        return new ApiResult(code, message, null, errors);
    }
    
    // 分页响应构建器
    public static ApiResult buildPageResult(Object records, long total, long currentPage, long pageSize) {
        Map<String, Object> paginationData = Map.of(
            "records", records,
            "total", total,
            "currentPage", currentPage,
            "pageSize", pageSize,
            "totalPages", (total + pageSize - 1) / pageSize
        );
        return ApiResult.success(paginationData);
    }
    
    // 错误详情内部类
    @Data
    public static class ErrorDetail {
        private String field;
        private String message;
        
        public ErrorDetail() {}
        
        public ErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}