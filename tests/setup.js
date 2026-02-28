import { config } from '@vue/test-utils'
import { vi } from 'vitest'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  }
}))

// 设置全局配置
config.global.mocks = {
  $t: (key) => key
}