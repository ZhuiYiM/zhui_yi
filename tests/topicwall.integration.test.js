import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import Topicwall from '@/components/user/Topicwall.vue';
import { topicAPI } from '@/api/topic';

// Mock the topic API
vi.mock('@/api/topic', () => ({
  topicAPI: {
    getTopics: vi.fn(),
    getPopularTags: vi.fn(),
    createTopic: vi.fn(),
    likeTopic: vi.fn()
  }
}));

describe('Topicwall Integration Test', () => {
  beforeEach(() => {
    // Clear all mocks before each test
    vi.clearAllMocks();
  });

  it('should fetch topics on component mount', async () => {
    // Mock API responses
    topicAPI.getTopics.mockResolvedValue({
      data: {
        topics: [
          {
            id: 1,
            content: 'Test topic content',
            author: { name: 'Test User', avatar: 'test-avatar.jpg' },
            likes: 10,
            comments: 5,
            isLiked: false,
            tags: ['test'],
            images: [],
            createdAt: new Date().toISOString()
          }
        ],
        total: 1,
        totalPosts: 100,
        todayPosts: 5
      }
    });

    topicAPI.getPopularTags.mockResolvedValue({
      data: {
        tags: ['校园生活', '学习经验']
      }
    });

    // Mount the component
    const wrapper = mount(Topicwall, {
      global: {
        stubs: {
          UnifiedNav: true
        }
      }
    });

    // Wait for async operations
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 100));

    // Verify API calls were made
    expect(topicAPI.getTopics).toHaveBeenCalled();
    expect(topicAPI.getPopularTags).toHaveBeenCalled();

    // Verify data is populated
    expect(wrapper.vm.posts.length).toBeGreaterThan(0);
    expect(wrapper.vm.popularTags.length).toBeGreaterThan(0);
    expect(wrapper.vm.stats.totalPosts).toBe(100);
  });

  it('should handle API errors gracefully', async () => {
    // Mock API error
    topicAPI.getTopics.mockRejectedValue(new Error('Network error'));
    topicAPI.getPopularTags.mockResolvedValue({
      data: {
        tags: ['校园生活', '学习经验']
      }
    });

    const wrapper = mount(Topicwall, {
      global: {
        stubs: {
          UnifiedNav: true
        }
      }
    });

    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 100));

    // Component should still render with fallback data
    expect(wrapper.exists()).toBe(true);
    // Popular tags should fall back to default values
    expect(wrapper.vm.popularTags.length).toBeGreaterThan(0);
  });

  it('should handle search functionality', async () => {
    const mockTopics = {
      data: {
        topics: [{
          id: 1,
          content: 'Search result',
          author: { name: 'Search User' },
          likes: 5,
          comments: 2,
          isLiked: false,
          tags: ['search'],
          images: [],
          createdAt: new Date().toISOString()
        }],
        total: 1
      }
    };

    topicAPI.getTopics.mockResolvedValue(mockTopics);

    const wrapper = mount(Topicwall, {
      global: {
        stubs: {
          UnifiedNav: true
        }
      }
    });

    await wrapper.vm.$nextTick();

    // Set search query and trigger search
    wrapper.vm.searchQuery = 'test search';
    await wrapper.vm.handleSearch();

    // Verify API was called with search parameters
    expect(topicAPI.getTopics).toHaveBeenCalledWith(
      expect.objectContaining({
        search: 'test search',
        page: 1
      })
    );
  });

  it('should handle tag filtering', async () => {
    topicAPI.getTopics.mockResolvedValue({
      data: {
        topics: [],
        total: 0
      }
    });

    const wrapper = mount(Topicwall, {
      global: {
        stubs: {
          UnifiedNav: true
        }
      }
    });

    await wrapper.vm.$nextTick();

    // Filter by tag
    await wrapper.vm.filterByTag('学习经验');

    // Verify API was called with tag parameter
    expect(topicAPI.getTopics).toHaveBeenCalledWith(
      expect.objectContaining({
        tag: '学习经验',
        page: 1
      })
    );
  });

  it('should handle topic publishing', async () => {
    topicAPI.createTopic.mockResolvedValue({
      data: { id: 1, content: 'New topic' }
    });

    topicAPI.getTopics.mockResolvedValue({
      data: {
        topics: [{ id: 1, content: 'New topic' }],
        total: 1
      }
    });

    const wrapper = mount(Topicwall, {
      global: {
        stubs: {
          UnifiedNav: true
        }
      }
    });

    await wrapper.vm.$nextTick();

    // Test publishing a topic
    const topicData = {
      content: 'Test topic content',
      tags: ['test'],
      images: []
    };

    await wrapper.vm.publishTopic(topicData);

    // Verify create API was called
    expect(topicAPI.createTopic).toHaveBeenCalledWith(topicData);
    // Verify topics were refreshed
    expect(topicAPI.getTopics).toHaveBeenCalledTimes(2);
  });
});