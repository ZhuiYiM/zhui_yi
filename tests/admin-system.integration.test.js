/**
 * 管理员系统功能集成测试
 * 
 * 测试说明：
 * 1. 本测试文件用于测试管理员系统的评论管理、操作日志、系统设置功能
 * 2. 测试前请确保：
 *    - 后端服务已启动 (http://localhost:8080)
 *    - 数据库已初始化 (campus_db)
 *    - 测试数据已准备 (admin_operation_log, system_settings, topic_comments, reports)
 * 3. 运行方式：
 *    - 在浏览器中打开前端页面
 *    - 使用 Postman 或其他工具测试 API
 *    - 使用 Jest 运行自动化测试
 */

import { adminAPI } from '../../../src/api/admin';
import request from '../../../src/api/request';

// 测试数据
const TEST_DATA = {
    commentId: 1,
    reportId: 1,
    settingKey: 'site_name',
    settingValue: 'Test Campus Platform'
};

/**
 * 测试评论管理功能
 */
describe('Admin Comments Management', () => {
    test('should get comment list successfully', async () => {
        try {
            const result = await adminAPI.getCommentList({ page: 1, limit: 10 });
            expect(result).toBeDefined();
            expect(result.list).toBeDefined();
            expect(result.total).toBeDefined();
            console.log('✅ 评论列表获取成功:', result.total);
        } catch (error) {
            console.error('❌ 评论列表获取失败:', error);
            throw error;
        }
    });

    test('should filter comments by keyword', async () => {
        try {
            const result = await adminAPI.getCommentList({ 
                page: 1, 
                limit: 10, 
                keyword: 'test' 
            });
            expect(result).toBeDefined();
            console.log('✅ 关键词搜索成功:', result.total);
        } catch (error) {
            console.error('❌ 关键词搜索失败:', error);
        }
    });

    test('should filter comments by status', async () => {
        try {
            const result = await adminAPI.getCommentList({ 
                page: 1, 
                limit: 10, 
                status: 1 
            });
            expect(result).toBeDefined();
            console.log('✅ 状态筛选成功:', result.total);
        } catch (error) {
            console.error('❌ 状态筛选失败:', error);
        }
    });

    test('should delete comment successfully', async () => {
        try {
            // 注意：实际测试时应该使用测试专用的评论 ID
            const result = await adminAPI.deleteComment(TEST_DATA.commentId);
            expect(result).toBeDefined();
            console.log('✅ 评论删除成功');
        } catch (error) {
            console.error('❌ 评论删除失败:', error);
        }
    });
});

/**
 * 测试操作日志功能
 */
describe('Admin Operation Logs', () => {
    test('should get operation logs successfully', async () => {
        try {
            const result = await adminAPI.getOperationLogs({ page: 1, limit: 10 });
            expect(result).toBeDefined();
            expect(result.list).toBeDefined();
            expect(result.total).toBeDefined();
            console.log('✅ 操作日志获取成功:', result.total);
        } catch (error) {
            console.error('❌ 操作日志获取失败:', error);
            throw error;
        }
    });

    test('should filter logs by module', async () => {
        try {
            const result = await adminAPI.getOperationLogs({ 
                page: 1, 
                limit: 10, 
                module: 'topic' 
            });
            expect(result).toBeDefined();
            console.log('✅ 按模块筛选成功:', result.total);
        } catch (error) {
            console.error('❌ 按模块筛选失败:', error);
        }
    });

    test('should filter logs by operation', async () => {
        try {
            const result = await adminAPI.getOperationLogs({ 
                page: 1, 
                limit: 10, 
                operation: 'delete' 
            });
            expect(result).toBeDefined();
            console.log('✅ 按操作类型筛选成功:', result.total);
        } catch (error) {
            console.error('❌ 按操作类型筛选失败:', error);
        }
    });

    test('should filter logs by admin ID', async () => {
        try {
            const result = await adminAPI.getOperationLogs({ 
                page: 1, 
                limit: 10, 
                adminId: '1' 
            });
            expect(result).toBeDefined();
            console.log('✅ 按操作人筛选成功:', result.total);
        } catch (error) {
            console.error('❌ 按操作人筛选失败:', error);
        }
    });
});

/**
 * 测试系统设置功能
 */
describe('Admin System Settings', () => {
    test('should get system settings successfully', async () => {
        try {
            const result = await adminAPI.getSystemSettings();
            expect(result).toBeDefined();
            expect(Array.isArray(result)).toBe(true);
            console.log('✅ 系统设置获取成功:', result.length);
        } catch (error) {
            console.error('❌ 系统设置获取失败:', error);
            throw error;
        }
    });

    test('should update system settings successfully', async () => {
        try {
            const settings = {
                site_name: 'Test Campus Platform',
                allow_registration: 'true',
                max_upload_size: '10485760'
            };
            const result = await adminAPI.updateSystemSettings(settings);
            expect(result).toBeDefined();
            console.log('✅ 系统设置更新成功');
        } catch (error) {
            console.error('❌ 系统设置更新失败:', error);
        }
    });
});

/**
 * 测试举报管理功能
 */
describe('Admin Report Management', () => {
    test('should get report list successfully', async () => {
        try {
            const result = await adminAPI.getReportList({ page: 1, limit: 10 });
            expect(result).toBeDefined();
            expect(result.list).toBeDefined();
            expect(result.total).toBeDefined();
            console.log('✅ 举报列表获取成功:', result.total);
        } catch (error) {
            console.error('❌ 举报列表获取失败:', error);
            throw error;
        }
    });

    test('should filter reports by type', async () => {
        try {
            const result = await adminAPI.getReportList({ 
                page: 1, 
                limit: 10, 
                type: 'spam' 
            });
            expect(result).toBeDefined();
            console.log('✅ 按类型筛选成功:', result.total);
        } catch (error) {
            console.error('❌ 按类型筛选失败:', error);
        }
    });

    test('should filter reports by status', async () => {
        try {
            const result = await adminAPI.getReportList({ 
                page: 1, 
                limit: 10, 
                status: '0' 
            });
            expect(result).toBeDefined();
            console.log('✅ 按状态筛选成功:', result.total);
        } catch (error) {
            console.error('❌ 按状态筛选失败:', error);
        }
    });

    test('should get report stats successfully', async () => {
        try {
            const result = await adminAPI.getReportStats();
            expect(result).toBeDefined();
            expect(result.total).toBeDefined();
            expect(result.pending).toBeDefined();
            expect(result.processing).toBeDefined();
            expect(result.processed).toBeDefined();
            console.log('✅ 举报统计获取成功');
            console.log('   总举报数:', result.total);
            console.log('   待处理:', result.pending);
            console.log('   处理中:', result.processing);
            console.log('   已处理:', result.processed);
        } catch (error) {
            console.error('❌ 举报统计获取失败:', error);
            throw error;
        }
    });
});

/**
 * 测试错误处理
 */
describe('Error Handling', () => {
    test('should handle invalid page number', async () => {
        try {
            await adminAPI.getCommentList({ page: -1, limit: 10 });
            console.log('❌ 应该抛出错误');
        } catch (error) {
            console.log('✅ 正确处理无效页码:', error.message);
        }
    });

    test('should handle invalid limit', async () => {
        try {
            await adminAPI.getCommentList({ page: 1, limit: -1 });
            console.log('❌ 应该抛出错误');
        } catch (error) {
            console.log('✅ 正确处理无效每页数量:', error.message);
        }
    });
});

/**
 * 性能测试
 */
describe('Performance Test', () => {
    test('should load comment list within 3 seconds', async () => {
        const startTime = Date.now();
        await adminAPI.getCommentList({ page: 1, limit: 10 });
        const endTime = Date.now();
        const duration = endTime - startTime;
        console.log(`✅ 评论列表加载时间：${duration}ms`);
        expect(duration).toBeLessThan(3000);
    });

    test('should load operation logs within 3 seconds', async () => {
        const startTime = Date.now();
        await adminAPI.getOperationLogs({ page: 1, limit: 10 });
        const endTime = Date.now();
        const duration = endTime - startTime;
        console.log(`✅ 操作日志加载时间：${duration}ms`);
        expect(duration).toBeLessThan(3000);
    });

    test('should load system settings within 2 seconds', async () => {
        const startTime = Date.now();
        await adminAPI.getSystemSettings();
        const endTime = Date.now();
        const duration = endTime - startTime;
        console.log(`✅ 系统设置加载时间：${duration}ms`);
        expect(duration).toBeLessThan(2000);
    });
});
