import { defineStore } from 'pinia'
import { ref } from 'vue'
import { API_BASE_URL } from '../config'

export const useUserStore = defineStore('user', () => {
    const isLoggedIn = ref(false)
    const isVip = ref(false)
    const userId = ref<number | null>(null)
    const uuid = ref<string | null>(null)
    const username = ref('Guest')
    const phone = ref('')
    // User type: NORMAL, MONTHLY_VIP, YEARLY_VIP
    const userType = ref<string>('NORMAL')

    // Mock login logic
    async function login(userPhone: string, code: string): Promise<boolean> {
        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ phone: userPhone, code })
            })

            if (response.ok) {
                const user = await response.json()
                isLoggedIn.value = true
                userId.value = user.id
                uuid.value = user.uuid
                username.value = user.nickname || '用户' + user.id
                // Store phone from API response, not from input
                phone.value = user.phone || userPhone
                isVip.value = user.isVip
                // Get user type from API response, default to NORMAL if not provided
                userType.value = user.userType || 'NORMAL'
                return true
            }
        } catch (e) {
            console.error(e)
        }
        return false
    }

    async function sendCode(userPhone: string): Promise<boolean> {
        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/send-code?phone=${userPhone}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            })
            return response.ok
        } catch (e) {
            console.error(e)
            return false
        }
    }

    function logout() {
        isLoggedIn.value = false
        userId.value = null
        uuid.value = null
        username.value = 'Guest'
        phone.value = ''
        isVip.value = false
        userType.value = 'NORMAL'
    }

    return { isLoggedIn, isVip, userId, uuid, username, phone, userType, login, logout, sendCode }
}, {
    persist: true
})
