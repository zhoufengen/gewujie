import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../utils/request'
import { useUserStore } from './userStore'

export const useLearningStore = defineStore('learning', () => {
    const userStore = useUserStore()
    const dailyNewWords = ref(0)
    const collectedWords = ref<string[]>([])
    const pendingReviewsCount = ref(0)
    const learningDates = ref<Record<string, string>>({}) // { dateString: 'learned' or 'checkedin' }
    const learnedRecords = ref<any[]>([]) // Detailed history: { learnedAt, character, textbookCategory }
    const hasSignedIn = ref(false) // 当前是否已打卡

    // Mock Lesson Data
    const currentLesson = ref<any>(null)

    async function fetchCurrentLesson(isGame: boolean = false) {
        try {
            let url = '/api/learning/lesson/current'
            const params = new URLSearchParams()
            if (isGame) params.append('isGame', 'true')

            if (params.toString()) {
                url += `?${params.toString()}`
            }

            const res = await api.get(url)
            if (res.ok) {
                const data = await res.json()
                currentLesson.value = data && Object.keys(data).length > 0 ? data : null
            } else {
                currentLesson.value = null
            }
        } catch (e) {
            console.error(e)
            currentLesson.value = null
        }
    }

    async function fetchCurrentLessonByTextbook(textbookCategory: string, isGame: boolean = false) {
        try {
            let url = `/api/learning/lesson/current/${encodeURIComponent(textbookCategory)}`
            const params = new URLSearchParams()
            if (isGame) params.append('isGame', 'true')

            if (params.toString()) {
                url += `?${params.toString()}`
            }

            const res = await api.get(url)
            if (res.ok) {
                const data = await res.json()
                currentLesson.value = data && Object.keys(data).length > 0 ? data : null
            } else {
                currentLesson.value = null
            }
        } catch (e) {
            console.error(e)
            currentLesson.value = null
        }
    }

    async function recordLearning(isGame: boolean = false) {
        if (!currentLesson.value) return

        if (isGame && !userStore.isVip) {
            const todayGameWords = await getTodayGameWordsCount()
            if (todayGameWords >= 1) {
                throw new Error('每日通过游戏只能获得1个新字')
            }
        }

        try {
            await api.post(`/api/learning/record?lessonId=${currentLesson.value.id}&isGame=${isGame}`)
            dailyNewWords.value++
            await fetchLearningDates()
        } catch (e) {
            console.error(e)
            throw e
        }
    }

    async function getTodayGameWordsCount(): Promise<number> {
        try {
            const res = await api.get('/api/stats/today-game-words')
            if (res.ok) {
                const data = await res.json()
                return data.count || 0
            }
            return 0
        } catch (e) {
            console.error(e)
            return 0
        }
    }

    async function fetchStats() {
        try {
            const res = await api.get('/api/stats/summary')
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

    async function fetchLearningDates() {
        try {
            const res = await api.get('/api/stats/learning-dates')
            console.log('API response status:', res.status)
            if (res.ok) {
                const data = await res.json()
                console.log('API response data:', data)
                learningDates.value = data
            }
        } catch (e) {
            console.error('Error fetching learning dates:', e)
        }
        console.log('Final learning dates:', learningDates.value)
    }

    async function checkIn() {
        try {
            const res = await api.post('/api/stats/check-in')
            if (res.ok) {
                const data = await res.json()
                hasSignedIn.value = true
                await fetchLearningDates()
                return data
            }
        } catch (e) {
            console.error(e)
        }
    }

    async function fetchSignInStatus() {
        try {
            const res = await api.get('/api/stats/is-checked-in-today')
            if (res.ok) {
                const data = await res.json()
                hasSignedIn.value = data.isCheckedIn
                return data.isCheckedIn
            }
        } catch (e) {
            console.error(e)
        }
    }

    async function fetchLearnedRecords() {
        try {
            const res = await api.get('/api/stats/learned-records')
            if (res.ok) {
                learnedRecords.value = await res.json()
            }
        } catch (e) {
            console.error(e)
        }
    }

    async function fetchLearningTrend() {
        try {
            const res = await api.get('/api/stats/learning-trend')
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
        currentLesson, learningDates, learnedRecords, hasSignedIn,
        fetchCurrentLesson, fetchCurrentLessonByTextbook,
        recordLearning, fetchStats, fetchLearningDates,
        fetchLearnedRecords,
        checkIn, fetchSignInStatus, fetchLearningTrend,
        setCurrentLesson
    }
})
