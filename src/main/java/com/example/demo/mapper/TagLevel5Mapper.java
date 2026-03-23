// mapper/TagLevel5Mapper.java
package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TagLevel5;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 五级标签 Mapper 接口
 */
@Mapper
public interface TagLevel5Mapper extends BaseMapper<TagLevel5> {
    
    /**
     * 根据代码列表查询标签
     */
    List<TagLevel5> selectByCodes(@Param("codes") List<String> codes);
    
    /**
     * 根据名称列表查询标签
     */
    List<TagLevel5> selectByNames(@Param("names") List<String> names);
}
