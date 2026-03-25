package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * 需要指定角色才能访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    
    /**
     * 需要的角色列表
     * @return 角色数组，如 ["admin", "user"]
     */
    String[] value() default {};
}
