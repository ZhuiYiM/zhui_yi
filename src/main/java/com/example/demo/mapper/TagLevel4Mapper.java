// mapper/TagLevel4Mapper.java
package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TagLevel4;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 四级标签 Mapper 接口
 */
@Mapper
public interface TagLevel4Mapper extends BaseMapper<TagLevel4> {
    
    /**
     * 根据名称列表查询标签
     */
    List<TagLevel4> selectByNames(@Param("names") List<String> names);
    
    /**
     * 批量插入或更新（忽略已存在的）
     */
    int insertOrUpdateIgnore(@Param("tags") List<TagLevel4> tags);
}
