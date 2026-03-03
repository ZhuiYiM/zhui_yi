package com.example.demo.exception;

import com.example.demo.common.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("参数验证失败: {}", e.getMessage());
        
        List<ApiResult.ErrorDetail> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(new ApiResult.ErrorDetail(error.getField(), error.getDefaultMessage()));
        }
        
        return ApiResult.error(400, "请求参数验证失败", errors);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleBindException(BindException e) {
        log.warn("参数绑定失败: {}", e.getMessage());
        
        List<ApiResult.ErrorDetail> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(new ApiResult.ErrorDetail(error.getField(), error.getDefaultMessage()));
        }
        
        return ApiResult.error(400, "参数绑定失败", errors);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束违反: {}", e.getMessage());
        
        List<ApiResult.ErrorDetail> errors = new ArrayList<>();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.add(new ApiResult.ErrorDetail(fieldName, violation.getMessage()));
        }
        
        return ApiResult.error(400, "参数约束违反", errors);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult handleException(Exception e) {
        log.error("系统异常: ", e);
        return ApiResult.error(500, "系统内部错误: " + e.getMessage());
    }

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResult handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理静态资源未找到异常（前后端分离架构下，前端路由不应由后端处理）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResult handleNoResourceFoundException(NoResourceFoundException e) {
        // 记录日志但不抛出错误，因为这是正常的前端路由请求
        log.debug("前端路由请求，由前端处理：{}", e.getResourcePath());
        return ApiResult.error(404, "资源未找到，请检查前端路由配置");
    }
}