package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * 需要指定身份才能访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireIdentity {
    
    /**
     * 需要的身份类型列表
     * @return 身份类型数组，如 ["student", "merchant", "organization"]
     */
    String[] value() default {};
    
    /**
     * 是否要求身份已认证
     * @return true-需要已认证，false-只要有该身份即可
     */
    boolean verified() default true;
}
