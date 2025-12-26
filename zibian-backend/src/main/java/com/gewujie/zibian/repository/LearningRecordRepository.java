package com.gewujie.zibian.repository;

import com.gewujie.zibian.model.LearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.time.LocalDateTime;

public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {
    List<LearningRecord> findByUserId(Long userId);
    List<LearningRecord> findByUserIdAndLessonId(Long userId, Long lessonId);
    long countByUserIdAndLearnedAtAfter(Long userId, LocalDateTime date);
    
    @Query("SELECT DISTINCT lr.lesson.character FROM LearningRecord lr WHERE lr.user.id = ?1 AND lr.learnedAt > ?2")
    List<String> findDistinctCharactersByUserIdAndLearnedAtAfter(Long userId, LocalDateTime date);
    
    @Query("SELECT lr FROM LearningRecord lr WHERE lr.user.id = ?1 AND lr.nextReviewDate <= ?2")
    List<LearningRecord> findPendingReviews(Long userId, LocalDateTime now);
    
    @Query("SELECT lr FROM LearningRecord lr WHERE lr.user.id = ?1 AND lr.nextReviewDate <= ?2 AND lr.lesson.textbookCategory = ?3")
    List<LearningRecord> findPendingReviewsByTextbookCategory(Long userId, LocalDateTime now, String textbookCategory);
}
