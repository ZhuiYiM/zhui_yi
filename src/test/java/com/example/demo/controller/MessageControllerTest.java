package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Message;
import com.example.demo.mapper.MessageMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息中心功能测试类
 * 测试范围：消息发送、接收、已读、删除等核心功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    private static String testUser1Token;
    private static String testUser2Token;
    private static Integer testUser1Id = 1001;
    private static Integer testUser2Id = 1002;

    /**
     * 测试前置准备：获取测试用户的 Token
     */
    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc) throws Exception {
        // 模拟登录获取 Token（实际项目中应该调用登录接口）
        // 这里使用测试数据中的用户 ID 直接生成 Token
        testUser1Token = "Bearer test_token_user_1001";
        testUser2Token = "Bearer test_token_user_1002";
    }

    /**
     * 测试 1：发送私信 - 成功场景
     */
    @Test
    @Order(1)
    @DisplayName("测试发送私信 - 成功")
    void testSendMessage_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("toUserId", testUser2Id);
        requestBody.put("content", "这是一条测试私信");
        requestBody.put("type", "private_message");

        MvcResult result = mockMvc.perform(post("/api/messages")
                .header("Authorization", testUser1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result responseResult = objectMapper.readValue(response, Result.class);
        
        assertEquals(200, responseResult.getCode());
        assertEquals("消息发送成功", responseResult.getMessage());
    }

    /**
     * 测试 2：发送私信 - 失败场景（内容为空）
     */
    @Test
    @Order(2)
    @DisplayName("测试发送私信 - 内容为空")
    void testSendMessage_EmptyContent() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("toUserId", testUser2Id);
        requestBody.put("content", "");
        requestBody.put("type", "private_message");

        MvcResult result = mockMvc.perform(post("/api/messages")
                .header("Authorization", testUser1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result responseResult = objectMapper.readValue(response, Result.class);
        
        assertNotEquals(200, responseResult.getCode());
        assertTrue(responseResult.getMessage().contains("不能为空"));
    }

    /**
     * 测试 3：获取消息列表 - 成功场景
     */
    @Test
    @Order(3)
    @DisplayName("测试获取消息列表 - 成功")
    void testGetMessages_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/messages")
                .header("Authorization", testUser2Token)
                .param("type", "private_message")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result responseResult = objectMapper.readValue(response, Result.class);
        
        assertEquals(200, responseResult.getCode());
        assertNotNull(responseResult.getData());
    }

    /**
     * 测试 4：获取消息列表 - 按类型过滤
     */
    @Test
    @Order(4)
    @DisplayName("测试获取系统消息")
    void testGetSystemMessages() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/messages")
                .header("Authorization", testUser1Token)
                .param("type", "system")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result responseResult = objectMapper.readValue(response, Result.class);
        
        assertEquals(200, responseResult.getCode());
    }

    /**
     * 测试 5：标记消息为已读 - 成功场景
     */
    @Test
    @Order(5)
    @DisplayName("测试标记消息为已读 - 成功")
    void testMarkAsRead_Success() throws Exception {
        // 先获取一条未读消息
        MvcResult listResult = mockMvc.perform(get("/api/messages")
                .header("Authorization", testUser1Token)
                .param("type", "private_message")
                .param("page", "1")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn();

        String listResponse = listResult.getResponse().getContentAsString();
        Result listResultObj = objectMapper.readValue(listResponse, Result.class);
        
        Map<String, Object> data = (Map<String, Object>) listResultObj.getData();
        List<Map<String, Object>> records = (List<Map<String, Object>>) ((Map<String, Object>) data.get("data")).get("records");
        
        if (!records.isEmpty()) {
            Integer messageId = (Integer) records.get(0).get("id");
            
            MvcResult result = mockMvc.perform(put("/api/messages/{id}/read", messageId)
                    .header("Authorization", testUser1Token))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result responseResult = objectMapper.readValue(response, Result.class);
            
            assertEquals(200, responseResult.getCode());
        }
    }

    /**
     * 测试 6：获取未读消息数量
     */
    @Test
    @Order(6)
    @DisplayName("测试获取未读消息数量")
    void testGetUnreadCount() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/messages/unread-count")
                .header("Authorization", testUser1Token))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result responseResult = objectMapper.readValue(response, Result.class);
        
        assertEquals(200, responseResult.getCode());
        assertNotNull(responseResult.getData());
    }

    /**
     * 测试 7：批量标记已读
     */
    @Test
    @Order(7)
    @DisplayName("测试批量标记消息为已读")
    void testMarkAsReadBatch() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ids", List.of(1, 2, 3));

        MvcResult result = mockMvc.perform(put("/api/messages/read-batch")
                .header("Authorization", testUser1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result responseResult = objectMapper.readValue(response, Result.class);
        
        assertEquals(200, responseResult.getCode());
    }

    /**
     * 测试 8：删除消息 - 成功场景
     */
    @Test
    @Order(8)
    @DisplayName("测试删除消息 - 成功")
    void testDeleteMessage_Success() throws Exception {
        // 创建一个测试消息
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("toUserId", testUser2Id);
        requestBody.put("content", "待删除的测试消息");
        requestBody.put("type", "private_message");

        MvcResult createResult = mockMvc.perform(post("/api/messages")
                .header("Authorization", testUser1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn();

        // 获取刚创建的消息 ID（实际应该从响应中获取，这里简化处理）
        // 假设消息 ID 为最新的一条
        MvcResult listResult = mockMvc.perform(get("/api/messages")
                .header("Authorization", testUser2Token)
                .param("type", "private_message")
                .param("page", "1")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn();

        String listResponse = listResult.getResponse().getContentAsString();
        Result listResultObj = objectMapper.readValue(listResponse, Result.class);
        
        Map<String, Object> data = (Map<String, Object>) listResultObj.getData();
        List<Map<String, Object>> records = (List<Map<String, Object>>) ((Map<String, Object>) data.get("data")).get("records");
        
        if (!records.isEmpty()) {
            Integer messageId = (Integer) records.get(0).get("id");
            
            MvcResult result = mockMvc.perform(delete("/api/messages/{id}", messageId)
                    .header("Authorization", testUser2Token))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result responseResult = objectMapper.readValue(response, Result.class);
            
            assertEquals(200, responseResult.getCode());
            assertEquals("消息删除成功", responseResult.getMessage());
        }
    }

    /**
     * 测试 9：发送私信 - 失败场景（未登录）
     */
    @Test
    @Order(9)
    @DisplayName("测试发送私信 - 未登录")
    void testSendMessage_Unauthorized() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("toUserId", testUser2Id);
        requestBody.put("content", "测试消息");
        requestBody.put("type", "private_message");

        MvcResult result = mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertTrue(result.getResponse().getStatus() >= 400);
    }

    /**
     * 测试 10：获取消息列表 - 分页测试
     */
    @Test
    @Order(10)
    @DisplayName("测试消息列表分页功能")
    void testGetMessages_Pagination() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/messages")
                .header("Authorization", testUser1Token)
                .param("type", "")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result responseResult = objectMapper.readValue(response, Result.class);
        
        assertEquals(200, responseResult.getCode());
        
        Map<String, Object> data = (Map<String, Object>) responseResult.getData();
        assertNotNull(data.get("records"));
        assertNotNull(data.get("total"));
        assertNotNull(data.get("current"));
        assertNotNull(data.get("size"));
    }
}
