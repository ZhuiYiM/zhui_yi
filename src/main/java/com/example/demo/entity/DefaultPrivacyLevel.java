package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("default_privacy_levels")
public class DefaultPrivacyLevel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String level; // public, friends, private
}