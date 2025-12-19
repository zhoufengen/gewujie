import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const isLoggedIn = ref(false)
    const isVip = ref(false)
    const username = ref('Guest')
    const phone = ref('')

    // Mock login logic
    async function login(userPhone: string, code: string): Promise<boolean> {
        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 500))

        // Accept any code for now
        if (code.length > 0) {
            isLoggedIn.value = true
            username.value = `User_${userPhone.slice(-4)}`
            phone.value = userPhone
            return true
        }
        return false
    }

    function logout() {
        isLoggedIn.value = false
        username.value = 'Guest'
        phone.value = ''
    }

    return { isLoggedIn, isVip, username, phone, login, logout }
})
