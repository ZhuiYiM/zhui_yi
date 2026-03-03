package com.example.demo.common;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Result {
    private Integer code;
    private String message;
    private Object data;
    private String timestamp;

    public Result(Integer code,String message,Object data){
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    public static Result success(){
        return new Result(200,"success",null);
    }
    public static Result success(Object data){
        return new Result(200,"success",data);
    }
    public static Result success(String message, Object data){
        return new Result(200,message,data);
    }
    public static Result error(Integer code,String message){
        return new Result(code,message,null);
    }
    public static Result error(String message){
        return new Result(500,message,null);
    }
    
    // 添加带错误详情的错误响应
    public static Result error(Integer code, String message, Object errors){
        Result result = new Result(code, message, null);
        // 这里可以添加错误详情字段
        return result;
    }

}
