import { defineStore } from 'pinia'
import { ref } from 'vue'
import { API_BASE_URL } from '../config'

export const useLearningStore = defineStore('learning', () => {
    const dailyNewWords = ref(0)
    const collectedWords = ref<string[]>([])
    const pendingReviewsCount = ref(0)
    const learningDates = ref<Record<string, string>>({}) // { dateString: 'learned' or 'checkedin' }
    const learnedRecords = ref<any[]>([]) // Detailed history: { learnedAt, character, textbookCategory }
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
            await fetchLearningDates(userId) // 更新学习日期数据
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
            console.log('Fetching learning dates for userId:', userId)
            const res = await fetch(`${API_BASE_URL}/api/stats/learning-dates?userId=${userId}`)
            console.log('API response status:', res.status)
            if (res.ok) {
                const data = await res.json()
                console.log('API response data:', data)
                learningDates.value = data

                // Add test data for today if not present
                const now = new Date()
                const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
                console.log('Today string:', today)
                console.log('Today in learning dates?', today in learningDates.value)
                if (!learningDates.value[today]) {
                    console.log('Adding test data for today')
                    learningDates.value[today] = 'learned' // Simulate learning today
                }
            }
        } catch (e) {
            console.error('Error fetching learning dates:', e)
            // Add fallback test data for today if API fails
            const now = new Date()
            const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
            learningDates.value[today] = 'learned'
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
