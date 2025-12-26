import { defineStore } from 'pinia'
import { ref } from 'vue'
import { API_BASE_URL } from '../config'

export const useLearningStore = defineStore('learning', () => {
    const dailyNewWords = ref(0)
    const collectedWords = ref<string[]>([])
    const pendingReviewsCount = ref(0)

    // Mock Lesson Data
    const currentLesson = ref<any>(null)

    async function fetchCurrentLesson() {
        try {
            const res = await fetch(`${API_BASE_URL}/api/learning/lesson/current`)
            if (res.ok) {
                currentLesson.value = await res.json()
            }
        } catch (e) {
            console.error(e)
        }
    }

    async function fetchCurrentLessonByTextbook(textbookCategory: string) {
        try {
            const res = await fetch(`${API_BASE_URL}/api/learning/lesson/current/${encodeURIComponent(textbookCategory)}`)
            if (res.ok) {
                currentLesson.value = await res.json()
            }
        } catch (e) {
            console.error(e)
        }
    }

    async function recordLearning(userId: number) {
        if (!currentLesson.value) return
        try {
            await fetch(`${API_BASE_URL}/api/learning/record?userId=${userId}&lessonId=${currentLesson.value.id}`, {
                method: 'POST'
            })
            dailyNewWords.value++ // Optimistic update
        } catch (e) {
            console.error(e)
        }
    }

    async function fetchStats(userId: number) {
        try {
            const res = await fetch(`${API_BASE_URL}/api/stats/summary?userId=${userId}`)
            if (res.ok) {
                const stats = await res.json()
                dailyNewWords.value = stats.dailyNewWords
                collectedWords.value = stats.collectedWords
                pendingReviewsCount.value = stats.pendingReviewsCount
            }
        } catch (e) {
            console.error(e)
        }
    }

    return { dailyNewWords, collectedWords, pendingReviewsCount, currentLesson, fetchCurrentLesson, fetchCurrentLessonByTextbook, recordLearning, fetchStats }
})
