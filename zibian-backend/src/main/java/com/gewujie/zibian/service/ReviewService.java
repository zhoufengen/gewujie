package com.gewujie.zibian.service;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.model.LearningRecord;
import com.gewujie.zibian.repository.LessonRepository;
import com.gewujie.zibian.repository.LearningRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private LearningRecordRepository learningRecordRepository;

    public List<Lesson> getReviewList(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<LearningRecord> pendingRecords = learningRecordRepository.findPendingReviews(userId, now);
        
        // Convert to lessons and return
        return pendingRecords.stream()
            .map(record -> record.getLesson())
            .collect(Collectors.toList());
    }
    
    public List<Lesson> getReviewListByTextbookCategory(Long userId, String textbookCategory) {
        LocalDateTime now = LocalDateTime.now();
        List<LearningRecord> pendingRecords = learningRecordRepository.findPendingReviewsByTextbookCategory(userId, now, textbookCategory);
        
        // Convert to lessons and return
        return pendingRecords.stream()
            .map(record -> record.getLesson())
            .collect(Collectors.toList());
    }

    public void submitReview(Long userId, Long lessonId, String rating) {
        // Find the learning record
        List<LearningRecord> records = learningRecordRepository.findByUserIdAndLessonId(userId, lessonId);
        if (records.isEmpty()) return;
        
        LearningRecord record = records.get(0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newReviewDate;
        
        // Calculate next review date based on rating (simple SuperMemo implementation)
        switch (rating.toLowerCase()) {
            case "forgot":
                // Forgotten: review tomorrow (1 day)
                newReviewDate = now.plusDays(1);
                break;
            case "hard":
                // Hard: review in 3 days
                newReviewDate = now.plusDays(3);
                break;
            case "easy":
                // Easy: review in 7 days
                newReviewDate = now.plusDays(7);
                break;
            default:
                // Default: same as hard
                newReviewDate = now.plusDays(3);
        }
        
        // Update and save
        record.setNextReviewDate(newReviewDate);
        learningRecordRepository.save(record);
    }
    
    public long getPendingReviewsCount(Long userId) {
        // Return count of distinct characters that need review
        LocalDateTime now = LocalDateTime.now();
        List<LearningRecord> pendingRecords = learningRecordRepository.findPendingReviews(userId, now);
        
        // Get distinct characters from pending reviews
        Set<String> distinctCharacters = pendingRecords.stream()
            .map(record -> record.getLesson().getCharacter())
            .collect(Collectors.toSet());
            
        return distinctCharacters.size();
    }
    
    public long getPendingReviewsCountByTextbookCategory(Long userId, String textbookCategory) {
        // Return count of distinct characters that need review for specific textbook category
        LocalDateTime now = LocalDateTime.now();
        List<LearningRecord> pendingRecords = learningRecordRepository.findPendingReviewsByTextbookCategory(userId, now, textbookCategory);
        
        // Get distinct characters from pending reviews
        Set<String> distinctCharacters = pendingRecords.stream()
            .map(record -> record.getLesson().getCharacter())
            .collect(Collectors.toSet());
            
        return distinctCharacters.size();
    }
}
