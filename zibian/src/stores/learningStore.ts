import { defineStore } from 'pinia'
import { ref } from 'vue'
import { API_BASE_URL } from '../config'
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

    async function fetchCurrentLesson(userId?: number, isGame: boolean = false) {
        try {
            let url = `${API_BASE_URL}/api/learning/lesson/current`
            const params = new URLSearchParams()
            if (userId) params.append('userId', userId.toString())
            if (isGame) params.append('isGame', 'true')

            if (params.toString()) {
                url += `?${params.toString()}`
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

    async function fetchCurrentLessonByTextbook(textbookCategory: string, userId?: number, isGame: boolean = false) {
        try {
            let url = `${API_BASE_URL}/api/learning/lesson/current/${encodeURIComponent(textbookCategory)}`
            const params = new URLSearchParams()
            if (userId) params.append('userId', userId.toString())
            if (isGame) params.append('isGame', 'true')

            if (params.toString()) {
                url += `?${params.toString()}`
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

    async function recordLearning(userId: number, isGame: boolean = false) {
        if (!currentLesson.value) return

        // Check if daily game word limit reached for non-VIP users
        if (isGame && !userStore.isVip) {
            const todayGameWords = await getTodayGameWordsCount(userId)
            if (todayGameWords >= 1) {
                throw new Error('每日通过游戏只能获得1个新字')
            }
        }

        try {
            await fetch(`${API_BASE_URL}/api/learning/record?userId=${userId}&lessonId=${currentLesson.value.id}&isGame=${isGame}`, {
                method: 'POST'
            })
            dailyNewWords.value++ // Optimistic update
            await fetchLearningDates(userId) // 更新学习日期数据
        } catch (e) {
            console.error(e)
            throw e // Re-throw to allow UI handling
        }
    }

    async function getTodayGameWordsCount(userId: number): Promise<number> {
        try {
            const res = await fetch(`${API_BASE_URL}/api/stats/today-game-words?userId=${userId}`)
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
            console.log('Fetching learning dates for userId:', userId)
            const res = await fetch(`${API_BASE_URL}/api/stats/learning-dates?userId=${userId}`)
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

    async function fetchLearnedRecords(userId: number) {
        try {
            const res = await fetch(`${API_BASE_URL}/api/stats/learned-records?userId=${userId}`)
            if (res.ok) {
                learnedRecords.value = await res.json()
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
        currentLesson, learningDates, learnedRecords, hasSignedIn,
        fetchCurrentLesson, fetchCurrentLessonByTextbook,
        recordLearning, fetchStats, fetchLearningDates,
        fetchLearnedRecords,
        checkIn, fetchSignInStatus, fetchLearningTrend,
        setCurrentLesson
    }
})
