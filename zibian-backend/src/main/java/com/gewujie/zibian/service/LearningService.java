package com.gewujie.zibian.service;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.model.LessonStyle;
import com.gewujie.zibian.repository.LessonRepository;
import com.gewujie.zibian.repository.LessonStyleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class LearningService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonStyleRepository lessonStyleRepository;

    public Lesson getLesson(Long id) {
        return lessonRepository.findById(id).orElse(null);
    }

    public Lesson getRandomLesson() {
        List<Lesson> all = lessonRepository.findAll();
        if (all.isEmpty())
            return null;
        return all.get(new Random().nextInt(all.size()));
    }

    @Transactional
    public Lesson createLesson(Lesson lesson) {
        Lesson saved = lessonRepository.save(lesson);
        if (lesson.getStyles() != null) {
            for (LessonStyle style : lesson.getStyles()) {
                style.setLesson(saved);
                lessonStyleRepository.save(style);
            }
        }
        return saved;
    }

    @Autowired
    private com.gewujie.zibian.repository.LearningRecordRepository learningRecordRepository;
    @Autowired
    private com.gewujie.zibian.repository.UserRepository userRepository;

    public void recordLearning(Long userId, Long lessonId) {
        // Simple record
        com.gewujie.zibian.model.LearningRecord record = new com.gewujie.zibian.model.LearningRecord();
        record.setUser(userRepository.findById(userId).orElse(null));
        record.setLesson(getLesson(lessonId));
        learningRecordRepository.save(record);
    }

    public long getDailyNewWords(Long userId) {
        // Return count of distinct characters learned today
        java.time.LocalDateTime today = java.time.LocalDate.now().atStartOfDay();
        return learningRecordRepository.findDistinctCharactersByUserIdAndLearnedAtAfter(userId, today).size();
    }

    @Transactional
    public boolean isDataReady() {
        Lesson l = getRandomLesson();
        if (l == null)
            return false;
        // Check styles AND words are present
        boolean hasStyles = l.getStyles() != null && !l.getStyles().isEmpty();
        boolean hasWords = l.getWords() != null && !l.getWords().isEmpty();
        return hasStyles && hasWords;
    }

    @Transactional
    public void deleteAllLessons() {
        lessonStyleRepository.deleteAll();
        lessonRepository.deleteAll();
    }

    @Transactional
    public Lesson findByCharacter(String character) {
        List<Lesson> lessons = lessonRepository.findAllByCharacter(character);
        if (lessons.isEmpty())
            return null;
        // Return first one; if there are duplicates, delete extras
        if (lessons.size() > 1) {
            System.out.println(
                    "Found " + lessons.size() + " duplicates for character: " + character + ", cleaning up...");
            for (int i = 1; i < lessons.size(); i++) {
                Lesson dup = lessons.get(i);
                // Delete by lesson ID to avoid lazy loading styles
                lessonStyleRepository.deleteByLessonId(dup.getId());
                lessonRepository.deleteById(dup.getId());
            }
        }
        return lessons.get(0);
    }

    @Transactional
    public Lesson updateLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    @Transactional(readOnly = true)
    public List<String> getLearnedCharacters(Long userId) {
        return learningRecordRepository.findByUserId(userId).stream()
                .map(record -> record.getLesson().getCharacter())
                .distinct()
                .toList();
    }
}
