import { useUserStore } from '../stores/userStore'
import { API_BASE_URL } from '../config'

// Promise to prevent concurrent refresh requests
let refreshPromise: Promise<boolean> | null = null

/**
 * Unified API request wrapper with automatic token refresh
 * @param url - Request URL (relative or absolute)
 * @param options - Fetch options
 * @returns Response
 */
export async function request(url: string, options: RequestInit = {}): Promise<Response> {
    const userStore = useUserStore()

    // Ensure headers object exists
    const headers = new Headers(options.headers || {})

    // Add token header if available
    if (userStore.token) {
        headers.set('satoken', userStore.token)
    }

    // Add Content-Type if not set and body exists
    if (options.body && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json')
    }

    // Make request with updated headers
    const finalOptions: RequestInit = {
        ...options,
        headers
    }

    // Construct full URL if relative
    const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`

    let response = await fetch(fullUrl, finalOptions)

    // Handle 401 - token expired
    if (response.status === 401) {
        // Try to refresh token
        const refreshed = await refreshAccessToken()

        if (refreshed) {
            // Retry original request with new token
            headers.set('satoken', userStore.token)
            const retryOptions: RequestInit = {
                ...options,
                headers
            }
            response = await fetch(fullUrl, retryOptions)
        } else {
            // Refresh failed - redirect to login
            userStore.logout()
            window.location.href = '/login'
            throw new Error('Session expired, please login again')
        }
    }

    return response
}

/**
 * Refresh access token using refresh token
 * @returns true if refresh successful, false otherwise
 */
async function refreshAccessToken(): Promise<boolean> {
    // If already refreshing, wait for that promise
    if (refreshPromise) {
        return refreshPromise
    }

    const userStore = useUserStore()

    // No refresh token available
    if (!userStore.refreshToken) {
        return false
    }

    // Create refresh promise to prevent concurrent requests
    refreshPromise = (async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    refreshToken: userStore.refreshToken
                })
            })

            if (response.ok) {
                const data = await response.json()
                // Update access token
                userStore.token = data.token
                return true
            } else {
                // Refresh token also expired or invalid
                return false
            }
        } catch (error) {
            console.error('Token refresh failed:', error)
            return false
        } finally {
            // Clear refresh promise
            refreshPromise = null
        }
    })()

    return refreshPromise
}

/**
 * Convenience methods for common HTTP methods
 */
export const api = {
    get: (url: string, options?: RequestInit) =>
        request(url, { ...options, method: 'GET' }),

    post: (url: string, body?: any, options?: RequestInit) =>
        request(url, {
            ...options,
            method: 'POST',
            body: body ? JSON.stringify(body) : undefined
        }),

    put: (url: string, body?: any, options?: RequestInit) =>
        request(url, {
            ...options,
            method: 'PUT',
            body: body ? JSON.stringify(body) : undefined
        }),

    delete: (url: string, options?: RequestInit) =>
        request(url, { ...options, method: 'DELETE' })
}
