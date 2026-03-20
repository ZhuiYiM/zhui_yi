package com.example.demo.controller.admin;

import com.example.demo.entity.admin.OperationLog;
import com.example.demo.entity.admin.SystemSetting;
import com.example.demo.entity.Comments;
import com.example.demo.entity.Report;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * 管理员系统功能测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AdminSystemTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试评论管理功能
     */
    @Test
    public void testCommentManagement() throws Exception {
        // 1. 测试分页查询评论列表
        mockMvc.perform(get("/admin/comments")
                .param("page", "1")
                .param("limit", "10")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());

        // 2. 测试带关键词搜索
        mockMvc.perform(get("/admin/comments")
                .param("page", "1")
                .param("limit", "10")
                .param("keyword", "test")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 测试带状态筛选
        mockMvc.perform(get("/admin/comments")
                .param("page", "1")
                .param("limit", "10")
                .param("status", "1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试操作日志功能
     */
    @Test
    public void testOperationLogs() throws Exception {
        // 1. 测试分页查询操作日志
        mockMvc.perform(get("/admin/logs")
                .param("page", "1")
                .param("limit", "10")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());

        // 2. 测试按模块筛选
        mockMvc.perform(get("/admin/logs")
                .param("page", "1")
                .param("limit", "10")
                .param("module", "topic")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 测试按操作类型筛选
        mockMvc.perform(get("/admin/logs")
                .param("page", "1")
                .param("limit", "10")
                .param("operation", "delete")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 测试按操作人 ID 筛选
        mockMvc.perform(get("/admin/logs")
                .param("page", "1")
                .param("limit", "10")
                .param("adminId", "1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试系统设置功能
     */
    @Test
    public void testSystemSettings() throws Exception {
        // 1. 测试获取系统设置
        mockMvc.perform(get("/admin/settings")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        // 2. 测试更新系统设置
        Map<String, String> settings = new HashMap<>();
        settings.put("site_name", "Test Campus Platform");
        settings.put("allow_registration", "true");
        settings.put("max_upload_size", "10485760");

        mockMvc.perform(put("/admin/settings")
                .contentType(APPLICATION_JSON)
                .content(asJsonString(settings)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试举报管理功能
     */
    @Test
    public void testReportManagement() throws Exception {
        // 1. 测试分页查询举报列表
        mockMvc.perform(get("/admin/reports")
                .param("page", "1")
                .param("limit", "10")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());

        // 2. 测试按类型筛选
        mockMvc.perform(get("/admin/reports")
                .param("page", "1")
                .param("limit", "10")
                .param("type", "spam")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 测试按状态筛选
        mockMvc.perform(get("/admin/reports")
                .param("page", "1")
                .param("limit", "10")
                .param("status", "0")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试举报统计功能
     */
    @Test
    public void testReportStats() throws Exception {
        // 测试获取举报统计
        mockMvc.perform(get("/admin/reports/stats")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.pending").isNumber())
                .andExpect(jsonPath("$.data.processing").isNumber())
                .andExpect(jsonPath("$.data.processed").isNumber());
    }

    /**
     * 测试删除评论功能
     */
    @Test
    public void testDeleteComment() throws Exception {
        // 先查询一条评论
        String response = mockMvc.perform(get("/admin/comments")
                .param("page", "1")
                .param("limit", "1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        // 解析 ID 并删除（这里简化处理，实际应该解析 JSON）
        // 测试删除接口
        mockMvc.perform(delete("/admin/comments/1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 辅助方法：将对象转换为 JSON 字符串
     */
    private String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
