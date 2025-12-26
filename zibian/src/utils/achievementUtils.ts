// 成就体系规则
// 根据已学汉字数量划分成就等级
export interface AchievementLevel {
  name: string;
  minWords: number;
  description: string;
}

export const ACHIEVEMENT_LEVELS: AchievementLevel[] = [
  { name: '启蒙', minWords: 0, description: '刚刚开始学习汉字之旅' },
  { name: '入门', minWords: 10, description: '掌握了基础汉字' },
  { name: '秀才', minWords: 50, description: '已经认识了不少汉字' },
  { name: '举人', minWords: 100, description: '汉字知识有了显著提升' },
  { name: '进士', minWords: 200, description: '对汉字有较深的理解' },
  { name: '探花', minWords: 300, description: '汉字造诣已经很高' },
  { name: '榜眼', minWords: 400, description: '接近汉字专家水平' },
  { name: '状元', minWords: 500, description: '汉字知识已臻化境' }
];

// 根据已学汉字数量获取当前成就
export function getCurrentAchievement(learnedWordsCount: number): AchievementLevel {
  // 从高到低查找匹配的成就等级
  for (let i = ACHIEVEMENT_LEVELS.length - 1; i >= 0; i--) {
    const level = ACHIEVEMENT_LEVELS[i];
    if (learnedWordsCount >= level.minWords) {
      return level;
    }
  }
  // 默认返回最低等级
  return ACHIEVEMENT_LEVELS[0];
}