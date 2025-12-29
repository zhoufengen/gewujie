import { defineStore } from 'pinia'
import { ref } from 'vue'
import { API_BASE_URL } from '../config'

export const useLearningStore = defineStore('learning', () => {
    const dailyNewWords = ref(0)
    const collectedWords = ref<string[]>([])
    const pendingReviewsCount = ref(0)
    const learningDates = ref<Record<string, string>>({}) // { dateString: 'learned' or 'checkedin' }
    const hasSignedIn = ref(false) // 当前是否已打卡

    // Mock Lesson Data
    const currentLesson = ref<any>(null)

    async function fetchCurrentLesson(userId?: number) {
        try {
            let url = `${API_BASE_URL}/api/learning/lesson/current`
            if (userId) {
                url += `?userId=${userId}`
            }
            const res = await fetch(url)
            if (res.ok) {
                const data = await res.json()
                // Handle case where backend returns null (empty JSON)
                currentLesson.value = data && Object.keys(data).length > 0 ? data : null
            } else {
                currentLesson.value = null
            }
        } catch (e) {
            console.error(e)
            currentLesson.value = null
        }
    }

    async function fetchCurrentLessonByTextbook(textbookCategory: string, userId?: number) {
        try {
            let url = `${API_BASE_URL}/api/learning/lesson/current/${encodeURIComponent(textbookCategory)}`
            if (userId) {
                url += `?userId=${userId}`
            }
            const res = await fetch(url)
            if (res.ok) {
                const data = await res.json()
                // Handle case where backend returns null (empty JSON)
                currentLesson.value = data && Object.keys(data).length > 0 ? data : null
            } else {
                currentLesson.value = null
            }
        } catch (e) {
            console.error(e)
            currentLesson.value = null
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
    
    async function fetchLearningDates(userId: number) {
        try {
            const res = await fetch(`${API_BASE_URL}/api/stats/learning-dates?userId=${userId}`)
            if (res.ok) {
                learningDates.value = await res.json()
            }
        } catch (e) {
            console.error(e)
        }
    }

    async function checkIn(userId: number) {
        try {
            const res = await fetch(`${API_BASE_URL}/api/stats/check-in?userId=${userId}`, {
                method: 'POST'
            })
            if (res.ok) {
                const data = await res.json()
                hasSignedIn.value = true
                await fetchLearningDates(userId) // 更新学习日期
                return data
            }
        } catch (e) {
            console.error(e)
        }
    }

    async function fetchSignInStatus(userId: number) {
        try {
            const res = await fetch(`${API_BASE_URL}/api/stats/is-checked-in-today?userId=${userId}`)
            if (res.ok) {
                const data = await res.json()
                hasSignedIn.value = data.isCheckedIn
                return data.isCheckedIn
            }
        } catch (e) {
            console.error(e)
        }
    }
    
    async function fetchLearningTrend(userId: number) {
        try {
            const res = await fetch(`${API_BASE_URL}/api/stats/learning-trend?userId=${userId}`)
            if (res.ok) {
                return await res.json()
            }
        } catch (e) {
            console.error(e)
        }
        return []
    }

    function setCurrentLesson(lesson: any) {
        currentLesson.value = lesson
    }

    return { 
        dailyNewWords, collectedWords, pendingReviewsCount, 
        currentLesson, learningDates, hasSignedIn, 
        fetchCurrentLesson, fetchCurrentLessonByTextbook, 
        recordLearning, fetchStats, fetchLearningDates, 
        checkIn, fetchSignInStatus, fetchLearningTrend, 
        setCurrentLesson 
    }
})
