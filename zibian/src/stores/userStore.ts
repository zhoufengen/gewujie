import { defineStore } from 'pinia'
import { ref } from 'vue'
import { API_BASE_URL } from '../config'
import { useLearningStore } from './learningStore'

export const useUserStore = defineStore('user', () => {
    const isLoggedIn = ref(false)
    const isVip = ref(false)
    const userId = ref<number | null>(null)
    const uuid = ref<string | null>(null)
    const username = ref('Guest')
    const phone = ref('')
    // User type: NORMAL, MONTHLY_VIP, YEARLY_VIP
    const userType = ref<string>('NORMAL')
    const token = ref<string>('')
    const refreshToken = ref<string>('')

    const vipExpirationDate = ref<string | null>(null)
    const hasChangedNickname = ref<boolean>(false)

    // Clear learning store data to avoid showing previous user's data
    function clearLearningStore() {
        try {
            const learningStore = useLearningStore()
            learningStore.dailyNewWords = 0
            learningStore.collectedWords = []
            learningStore.pendingReviewsCount = 0
            learningStore.learningDates = {}
            learningStore.learnedRecords = []
            learningStore.hasSignedIn = false
            learningStore.currentLesson = null
        } catch (e) {
            console.error('Error clearing learning store:', e)
        }
    }

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
                phone.value = user.phone || userPhone
                isVip.value = user.isVip
                userType.value = user.userType || 'NORMAL'
                vipExpirationDate.value = user.vipExpirationDate
                hasChangedNickname.value = user.hasChangedNickname || false
                token.value = user.token || ''
                refreshToken.value = user.refreshToken || ''
                
                // Clear learning store data on login to avoid showing previous user's data
                clearLearningStore()
                
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

    async function updateNickname(newNickname: string): Promise<{ success: boolean; message?: string }> {
        if (!userId.value) return { success: false, message: '请先登录' }
        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/update-nickname?nickname=${encodeURIComponent(newNickname)}`, {
                headers: { 'satoken': token.value },
                method: 'POST'
            })
            if (response.ok) {
                const user = await response.json()
                username.value = user.nickname
                hasChangedNickname.value = true
                return { success: true }
            } else {
                const errorText = await response.text()
                return { success: false, message: errorText || '修改失败' }
            }
        } catch (e) {
            console.error(e)
            return { success: false, message: '网络错误' }
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
        vipExpirationDate.value = null
        hasChangedNickname.value = false
        token.value = ''
        refreshToken.value = ''
        
        // Clear learning store data on logout
        clearLearningStore()
    }

    return { isLoggedIn, isVip, userId, uuid, username, phone, userType, vipExpirationDate, hasChangedNickname, token, refreshToken, login, logout, sendCode, updateNickname }
}, {
    persist: true
})
