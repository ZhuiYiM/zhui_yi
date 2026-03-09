package com.example.demo.service;

import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.IdentityLevel1;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.impl.IdentityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 身份识别服务测试类
 */
public class IdentityServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private IdentityServiceImpl identityService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDetermineAdminIdentity() {
        // 准备测试数据
        User user = new User();
        user.setId(1);
        user.setRole("admin");

        when(userMapper.selectById(1)).thenReturn(user);

        // 执行测试
        String level1Tag = identityService.determineUserLevel1Tag(1);

        // 验证结果
        assertEquals(IdentityLevel1.ADMIN.getCode(), level1Tag);
    }

    @Test
    public void testDetermineStudentIdentity() {
        // 准备测试数据
        User user = new User();
        user.setId(2);
        user.setStudentId("2023123456");
        user.setRealName("张三");

        when(userMapper.selectById(2)).thenReturn(user);

        // 执行测试
        String level1Tag = identityService.determineUserLevel1Tag(2);

        // 验证结果
        assertEquals(IdentityLevel1.STUDENT.getCode(), level1Tag);
    }

    @Test
    public void testDetermineMerchantIdentity() {
        // 准备测试数据
        User user = new User();
        user.setId(3);
        user.setIsMerchant(1);

        when(userMapper.selectById(3)).thenReturn(user);

        // 执行测试
        String level1Tag = identityService.determineUserLevel1Tag(3);

        // 验证结果
        assertEquals(IdentityLevel1.MERCHANT.getCode(), level1Tag);
    }

    @Test
    public void testDetermineOrganizationIdentity() {
        // 准备测试数据
        User user = new User();
        user.setId(4);
        user.setIsOrganization(1);

        when(userMapper.selectById(4)).thenReturn(user);

        // 执行测试
        String level1Tag = identityService.determineUserLevel1Tag(4);

        // 验证结果
        assertEquals(IdentityLevel1.ORGANIZATION.getCode(), level1Tag);
    }

    @Test
    public void testDetermineSocietyIdentity() {
        // 准备测试数据
        User user = new User();
        user.setId(5);
        user.setIsRealNameVerified(1);

        when(userMapper.selectById(5)).thenReturn(user);

        // 执行测试
        String level1Tag = identityService.determineUserLevel1Tag(5);

        // 验证结果
        assertEquals(IdentityLevel1.SOCIETY.getCode(), level1Tag);
    }

    @Test
    public void testDefaultIdentity() {
        // 准备测试数据 - 没有任何认证的用户
        User user = new User();
        user.setId(6);

        when(userMapper.selectById(6)).thenReturn(user);

        // 执行测试
        String level1Tag = identityService.determineUserLevel1Tag(6);

        // 验证结果 - 应该默认为社会
        assertEquals(IdentityLevel1.SOCIETY.getCode(), level1Tag);
    }

    @Test
    public void testPriorityAdminOverStudent() {
        // 准备测试数据 - 同时是管理员和学生
        User user = new User();
        user.setId(7);
        user.setRole("admin");
        user.setStudentId("2023123456");

        when(userMapper.selectById(7)).thenReturn(user);

        // 执行测试
        String level1Tag = identityService.determineUserLevel1Tag(7);

        // 验证结果 - 管理员优先级更高
        assertEquals(IdentityLevel1.ADMIN.getCode(), level1Tag);
    }

    @Test
    public void testGetIdentityInfo() {
        // 准备测试数据
        User user = new User();
        user.setId(8);
        user.setStudentId("2023123456");
        user.setRealName("张三");
        user.setIsRealNameVerified(1);

        when(userMapper.selectById(8)).thenReturn(user);

        // 执行测试
        Result result = identityService.getIdentityInfo(8);

        // 验证结果
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }
}
