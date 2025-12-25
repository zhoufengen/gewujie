import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../userStore'

// Mock fetch
global.fetch = vi.fn()

describe('UserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  /**
   * Test login stores uuid from response
   * Validates: Requirements 3.1
   */
  it('should store uuid from API response on login', async () => {
    const mockUser = {
      id: 1,
      uuid: '550e8400-e29b-41d4-a716-446655440000',
      nickname: 'TestUser',
      phone: '13800138000',
      isVip: false,
      createdAt: '2024-01-01T00:00:00',
      updatedAt: '2024-01-01T00:00:00'
    }

    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUser
    })

    const store = useUserStore()
    const result = await store.login('13800138000', '123456')

    expect(result).toBe(true)
    expect(store.uuid).toBe('550e8400-e29b-41d4-a716-446655440000')
    expect(store.isLoggedIn).toBe(true)
  })

  /**
   * Test login stores phone from response
   * Validates: Requirements 3.2
   */
  it('should store phone from API response on login', async () => {
    const mockUser = {
      id: 1,
      uuid: '550e8400-e29b-41d4-a716-446655440000',
      nickname: 'TestUser',
      phone: '13800138000',
      isVip: false,
      createdAt: '2024-01-01T00:00:00',
      updatedAt: '2024-01-01T00:00:00'
    }

    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUser
    })

    const store = useUserStore()
    await store.login('13800138000', '123456')

    expect(store.phone).toBe('13800138000')
  })

  /**
   * Test login uses phone from response, not input
   */
  it('should use phone from API response, not from input', async () => {
    const mockUser = {
      id: 1,
      uuid: '550e8400-e29b-41d4-a716-446655440000',
      nickname: 'TestUser',
      phone: '13900139000', // Different from input
      isVip: false,
      createdAt: '2024-01-01T00:00:00',
      updatedAt: '2024-01-01T00:00:00'
    }

    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUser
    })

    const store = useUserStore()
    await store.login('13800138000', '123456')

    // Should use phone from response, not input
    expect(store.phone).toBe('13900139000')
  })

  /**
   * Test login handles null phone gracefully
   */
  it('should handle null phone from API response', async () => {
    const mockUser = {
      id: 1,
      uuid: '550e8400-e29b-41d4-a716-446655440000',
      nickname: 'WeChatUser',
      phone: null,
      isVip: false,
      createdAt: '2024-01-01T00:00:00',
      updatedAt: '2024-01-01T00:00:00'
    }

    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUser
    })

    const store = useUserStore()
    await store.login('13800138000', '123456')

    // Should fallback to input phone when response phone is null
    expect(store.phone).toBe('13800138000')
    expect(store.uuid).toBe('550e8400-e29b-41d4-a716-446655440000')
  })

  /**
   * Test logout clears uuid
   * Validates: Requirements 3.1
   */
  it('should clear uuid on logout', async () => {
    const mockUser = {
      id: 1,
      uuid: '550e8400-e29b-41d4-a716-446655440000',
      nickname: 'TestUser',
      phone: '13800138000',
      isVip: false,
      createdAt: '2024-01-01T00:00:00',
      updatedAt: '2024-01-01T00:00:00'
    }

    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUser
    })

    const store = useUserStore()
    await store.login('13800138000', '123456')

    expect(store.uuid).toBe('550e8400-e29b-41d4-a716-446655440000')

    store.logout()

    expect(store.uuid).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(store.userId).toBeNull()
    expect(store.phone).toBe('')
  })

  /**
   * Test login stores all user data
   */
  it('should store all user data on successful login', async () => {
    const mockUser = {
      id: 1,
      uuid: '550e8400-e29b-41d4-a716-446655440000',
      nickname: 'TestUser',
      phone: '13800138000',
      isVip: true,
      createdAt: '2024-01-01T00:00:00',
      updatedAt: '2024-01-01T00:00:00'
    }

    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUser
    })

    const store = useUserStore()
    const result = await store.login('13800138000', '123456')

    expect(result).toBe(true)
    expect(store.isLoggedIn).toBe(true)
    expect(store.userId).toBe(1)
    expect(store.uuid).toBe('550e8400-e29b-41d4-a716-446655440000')
    expect(store.username).toBe('TestUser')
    expect(store.phone).toBe('13800138000')
    expect(store.isVip).toBe(true)
  })

  /**
   * Test login returns false on failure
   */
  it('should return false on login failure', async () => {
    ;(global.fetch as any).mockResolvedValueOnce({
      ok: false
    })

    const store = useUserStore()
    const result = await store.login('13800138000', 'invalid')

    expect(result).toBe(false)
    expect(store.isLoggedIn).toBe(false)
    expect(store.uuid).toBeNull()
  })

  /**
   * Test login handles network errors
   */
  it('should handle network errors gracefully', async () => {
    ;(global.fetch as any).mockRejectedValueOnce(new Error('Network error'))

    const store = useUserStore()
    const result = await store.login('13800138000', '123456')

    expect(result).toBe(false)
    expect(store.isLoggedIn).toBe(false)
  })

  /**
   * Test sendCode calls correct endpoint
   */
  it('should call send-code endpoint', async () => {
    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true
    })

    const store = useUserStore()
    const result = await store.sendCode('13800138000')

    expect(result).toBe(true)
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/auth/send-code?phone=13800138000'),
      expect.objectContaining({
        method: 'POST'
      })
    )
  })

  /**
   * Test multiple login/logout cycles
   */
  it('should handle multiple login/logout cycles', async () => {
    const mockUser = {
      id: 1,
      uuid: '550e8400-e29b-41d4-a716-446655440000',
      nickname: 'TestUser',
      phone: '13800138000',
      isVip: false,
      createdAt: '2024-01-01T00:00:00',
      updatedAt: '2024-01-01T00:00:00'
    }

    const store = useUserStore()

    // First login
    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => mockUser
    })
    await store.login('13800138000', '123456')
    expect(store.uuid).toBe('550e8400-e29b-41d4-a716-446655440000')

    // Logout
    store.logout()
    expect(store.uuid).toBeNull()

    // Second login
    ;(global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ ...mockUser, uuid: '550e8400-e29b-41d4-a716-446655440001' })
    })
    await store.login('13800138000', '123456')
    expect(store.uuid).toBe('550e8400-e29b-41d4-a716-446655440001')
  })
})
