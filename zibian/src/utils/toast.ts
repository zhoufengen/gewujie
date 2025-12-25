import { ref } from 'vue'

export interface Toast {
    id: number
    message: string
    type: 'info' | 'success' | 'warning' | 'error'
    duration?: number
}

const toasts = ref<Toast[]>([])
let idCounter = 0

export function useToast() {
    const addToast = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info', duration = 3000) => {
        const id = idCounter++
        toasts.value.push({ id, message, type, duration })

        setTimeout(() => {
            removeToast(id)
        }, duration)
    }

    const removeToast = (id: number) => {
        toasts.value = toasts.value.filter(t => t.id !== id)
    }

    return {
        toasts,
        info: (msg: string) => addToast(msg, 'info'),
        success: (msg: string) => addToast(msg, 'success'),
        warning: (msg: string) => addToast(msg, 'warning'),
        error: (msg: string) => addToast(msg, 'error')
    }
}
