// entity/dto/TopicTagsDTO.java
package com.example.demo.entity.dto;

import lombok.Data;
import java.util.List;

/**
 * 话题标签数据传输对象
 * 用于发布和更新话题时传递四级标签信息
 */
@Data
public class TopicTagsDTO {
    
    /**
     * 一级标签（用户身份，单选）
     */
    private String level1Code;
    private String level1Name;
    
    /**
     * 二级标签（话题分类，多选）
     */
    private List<String> level2Codes;
    private List<String> level2Names;
    
    /**
     * 三级标签（地点实体，多选）
     */
    private List<String> level3Codes;
    private List<String> level3Names;
    
    /**
     * 四级标签（自定义标签，多选）
     */
    private List<Long> level4Ids;
    private List<String> level4Names;
}
