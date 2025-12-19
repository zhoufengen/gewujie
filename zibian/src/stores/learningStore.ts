import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLearningStore = defineStore('learning', () => {
    const dailyNewWords = ref(0)
    const collectedWords = ref<string[]>([])

    // Mock Lesson Data
    const currentLesson = ref({
        char: '永',
        pinyin: 'yǒng',
        definition: 'Water flowing long and deep; eternal, forever, always.',
        styles: [
            { name: '甲骨文', img: 'https://via.placeholder.com/150?text=Oracle' },
            { name: '金文', img: 'https://via.placeholder.com/150?text=Bronze' },
            { name: '小篆', img: 'https://via.placeholder.com/150?text=Seal' },
            { name: '隶书', img: 'https://via.placeholder.com/150?text=Clerical' },
            { name: '楷书', img: 'https://via.placeholder.com/150?text=Regular' },
            { name: '草书', img: 'https://via.placeholder.com/150?text=Cursive' },
            { name: '行书', img: 'https://via.placeholder.com/150?text=Running' },
        ],
        words: ['永远', '永恒', '永生']
    })

    return { dailyNewWords, collectedWords, currentLesson }
})
