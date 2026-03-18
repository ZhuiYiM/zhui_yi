/**
 * 消息中心前端功能测试
 * 测试范围：消息列表展示、私信对话、标记已读、删除消息等
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import Message from '@/components/user/Message.vue';
import { messageAPI } from '@/api/message';

// Mock API 调用
vi.mock('@/api/message', () => ({
    messageAPI: {
        getMessages: vi.fn(),
        getUnreadCount: vi.fn(),
        markAsRead: vi.fn(),
        markAsReadBatch: vi.fn(),
        deleteMessage: vi.fn()
    }
}));

// Mock 路由
vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: vi.fn()
    })
}));

// Mock Element Plus
vi.mock('element-plus', () => ({
    ElMessage: {
        success: vi.fn(),
        error: vi.fn(),
        warning: vi.fn(),
        info: vi.fn()
    }
}));

describe('Message.vue - 消息中心组件', () => {
    
    // 模拟的私信数据
    const mockPrivateMessages = {
        data: {
            records: [
                {
                    id: 1,
                    senderId: 1002,
                    senderName: 'test_user2',
                    senderAvatar: 'https://placehold.co/50x50/67C23A/FFFFFF?text=U2',
                    content: '你好啊！',
                    messageType: 'private_message',
                    isRead: 0,
                    createdAt: new Date().toISOString()
                },
                {
                    id: 2,
                    senderId: 1002,
                    senderName: 'test_user2',
                    senderAvatar: 'https://placehold.co/50x50/67C23A/FFFFFF?text=U2',
                    content: '好呀，几点钟？',
                    messageType: 'private_message',
                    isRead: 0,
                    createdAt: new Date(Date.now() - 1800000).toISOString() // 30 分钟前
                },
                {
                    id: 3,
                    senderId: 1003,
                    senderName: 'test_user3',
                    senderAvatar: 'https://placehold.co/50x50/F56C6C/FFFFFF?text=U3',
                    content: '你好，我想咨询一下学习问题',
                    messageType: 'private_message',
                    isRead: 0,
                    createdAt: new Date(Date.now() - 7200000).toISOString() // 2 小时前
                }
            ],
            total: 3,
            current: 1,
            size: 20
        }
    };

    // 模拟的系统消息数据
    const mockSystemMessages = {
        data: {
            records: [
                {
                    id: 10,
                    senderId: 1,
                    senderName: '系统管理员',
                    senderAvatar: 'https://placehold.co/50x50/4A90E2/FFFFFF?text=S',
                    content: '欢迎加入校园信息平台！',
                    messageType: 'system',
                    isRead: 1,
                    createdAt: new Date(Date.now() - 604800000).toISOString() // 7 天前
                }
            ],
            total: 1,
            current: 1,
            size: 20
        }
    };

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.setItem('user', JSON.stringify({
            id: 1001,
            username: 'test_user1',
            avatar: 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U1'
        }));
    });

    describe('基础功能测试', () => {
        
        it('应该成功渲染消息中心页面', async () => {
            messageAPI.getMessages.mockResolvedValue({ data: { records: [] } });
            messageAPI.getUnreadCount.mockResolvedValue({ data: 0 });

            const wrapper = mount(Message);
            await wrapper.vm.$nextTick();

            expect(wrapper.find('.message-page').exists()).toBe(true);
            expect(wrapper.find('.header h1').text()).toBe('消息中心');
        });

        it('应该显示消息类型标签', async () => {
            messageAPI.getMessages.mockResolvedValue({ data: { records: [] } });
            messageAPI.getUnreadCount.mockResolvedValue({ data: 0 });

            const wrapper = mount(Message);
            await wrapper.vm.$nextTick();

            const tabs = wrapper.findAll('.tab-item');
            expect(tabs.length).toBeGreaterThanOrEqual(4);
            
            const tabNames = tabs.map(tab => tab.find('.tab-name').text());
            expect(tabNames).toContain('全部');
            expect(tabNames).toContain('系统通知');
            expect(tabNames).toContain('私信');
            expect(tabNames).toContain('群聊');
        });
    });

    describe('私信对话功能测试', () => {
        
        it('应该正确聚合私信对话', async () => {
            messageAPI.getMessages.mockResolvedValue(mockPrivateMessages);
            messageAPI.getUnreadCount.mockResolvedValue({ data: 2 });

            const wrapper = mount(Message);
            wrapper.vm.activeTab = 'personal';
            await wrapper.vm.$nextTick();
            await wrapper.vm.fetchPrivateMessages();
            await wrapper.vm.$nextTick();

            // 验证聚合后的对话数量（应该是 2 个不同的发送者）
            expect(wrapper.vm.privateConversations.length).toBe(2);
            
            // 验证第一个对话的数据
            const firstConv = wrapper.vm.privateConversations[0];
            expect(firstConv.senderId).toBe(1002);
            expect(firstConv.unreadCount).toBe(2);
        });

        it('应该显示私信对话栏', async () => {
            messageAPI.getMessages.mockResolvedValue(mockPrivateMessages);
            messageAPI.getUnreadCount.mockResolvedValue({ data: 2 });

            const wrapper = mount(Message);
            wrapper.vm.activeTab = 'personal';
            await wrapper.vm.$nextTick();
            await wrapper.vm.fetchPrivateMessages();
            await wrapper.vm.$nextTick();

            const privateMessagesList = wrapper.find('.private-messages-list');
            expect(privateMessagesList.exists()).toBe(true);

            const conversationItems = wrapper.findAll('.private-message-item');
            expect(conversationItems.length).toBe(2);
        });

        it('应该正确显示未读消息徽章', async () => {
            messageAPI.getMessages.mockResolvedValue(mockPrivateMessages);
            messageAPI.getUnreadCount.mockResolvedValue({ data: 2 });

            const wrapper = mount(Message);
            wrapper.vm.activeTab = 'personal';
            await wrapper.vm.$nextTick();
            await wrapper.vm.fetchPrivateMessages();
            await wrapper.vm.$nextTick();

            const unreadBadge = wrapper.find('.unread-count-badge');
            expect(unreadBadge.exists()).toBe(true);
            expect(unreadBadge.text()).toBe('2');
        });

        it('应该标记未读对话样式', async () => {
            messageAPI.getMessages.mockResolvedValue(mockPrivateMessages);
            messageAPI.getUnreadCount.mockResolvedValue({ data: 2 });

            const wrapper = mount(Message);
            wrapper.vm.activeTab = 'personal';
            await wrapper.vm.$nextTick();
            await wrapper.vm.fetchPrivateMessages();
            await wrapper.vm.$nextTick();

            const unreadItem = wrapper.find('.private-message-item.unread');
            expect(unreadItem.exists()).toBe(true);
        });
    });

    describe('消息操作功能测试', () => {
        
        it('点击私信对话应该标记为已读', async () => {
            messageAPI.getMessages.mockResolvedValue(mockPrivateMessages);
            messageAPI.getUnreadCount.mockResolvedValue({ data: 2 });
            messageAPI.markAsReadBatch.mockResolvedValue({ data: '批量标记成功' });

            const wrapper = mount(Message);
            wrapper.vm.activeTab = 'personal';
            await wrapper.vm.$nextTick();
            await wrapper.vm.fetchPrivateMessages();
            await wrapper.vm.$nextTick();

            // 点击第一个对话
            const conversationItem = wrapper.find('.private-message-item');
            await conversationItem.trigger('click');

            // 验证调用了标记已读 API
            expect(messageAPI.markAsReadBatch).toHaveBeenCalled();
        });

        it('切换标签应该加载对应类型的消息', async () => {
            messageAPI.getMessages.mockResolvedValue(mockPrivateMessages);
            messageAPI.getUnreadCount.mockResolvedValue({ data: 2 });

            const wrapper = mount(Message);
            await wrapper.vm.$nextTick();

            // 切换到系统消息标签
            const systemTab = wrapper.findAll('.tab-item')[1];
            await systemTab.trigger('click');

            expect(wrapper.vm.activeTab).toBe('system');
            expect(messageAPI.getMessages).toHaveBeenCalledWith(
                expect.objectContaining({
                    type: 'system'
                })
            );
        });

        it('应该格式化消息时间', () => {
            const wrapper = mount(Message);
            
            const now = new Date();
            const yesterday = new Date(now.getTime() - 86400000);
            const lastWeek = new Date(now.getTime() - 604800000);

            expect(wrapper.vm.formatTime(now)).toMatch(/\d{2}:\d{2}/);
            expect(wrapper.vm.formatTime(yesterday)).toBe('昨天');
            expect(wrapper.vm.formatTime(lastWeek)).toMatch(/\d{1,2}天前/);
        });
    });

    describe('搜索功能测试', () => {
        
        it('搜索框应该可以输入', async () => {
            messageAPI.getMessages.mockResolvedValue({ data: { records: [] } });
            messageAPI.getUnreadCount.mockResolvedValue({ data: 0 });

            const wrapper = mount(Message);
            await wrapper.vm.$nextTick();

            const searchInput = wrapper.find('.search-bar input');
            await searchInput.setValue('测试搜索');

            expect(searchInput.element.value).toBe('测试搜索');
        });

        it('回车应该触发搜索', async () => {
            messageAPI.getMessages.mockResolvedValue({ data: { records: [] } });
            messageAPI.getUnreadCount.mockResolvedValue({ data: 0 });

            const wrapper = mount(Message);
            await wrapper.vm.$nextTick();

            const searchInput = wrapper.find('.search-bar input');
            await searchInput.setValue('测试');
            await searchInput.trigger('keyup.enter');

            expect(wrapper.vm.searchQuery).toBe('测试');
        });
    });

    describe('错误处理测试', () => {
        
        it('API 调用失败时应该显示错误提示', async () => {
            messageAPI.getMessages.mockRejectedValue(new Error('网络错误'));
            messageAPI.getUnreadCount.mockRejectedValue(new Error('网络错误'));

            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

            const wrapper = mount(Message);
            await wrapper.vm.$nextTick();

            expect(consoleSpy).toHaveBeenCalled();
            consoleSpy.mockRestore();
        });

        it('消息数据为空时应该正常处理', async () => {
            messageAPI.getMessages.mockResolvedValue({ data: { records: [] } });
            messageAPI.getUnreadCount.mockResolvedValue({ data: 0 });

            const wrapper = mount(Message);
            await wrapper.vm.$nextTick();

            expect(wrapper.vm.messages.length).toBe(0);
            expect(wrapper.vm.privateConversations.length).toBe(0);
        });
    });

    describe('响应式布局测试', () => {
        
        it('移动端应该显示不同的布局', async () => {
            // 模拟移动端窗口宽度
            Object.defineProperty(window, 'innerWidth', {
                writable: true,
                configurable: true,
                value: 375
            });

            messageAPI.getMessages.mockResolvedValue({ data: { records: [] } });
            messageAPI.getUnreadCount.mockResolvedValue({ data: 0 });

            const wrapper = mount(Message);
            await wrapper.vm.$nextTick();

            expect(wrapper.vm.isMobile).toBe(true);
            expect(wrapper.classes()).toContain('mobile');
        });
    });
});
