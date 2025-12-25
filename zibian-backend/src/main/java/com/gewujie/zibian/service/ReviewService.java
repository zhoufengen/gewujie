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
        // Mock logic: return random 5 lessons for review
        // In real app, query LearningRecord where nextReviewDate <= now
        List<Lesson> all = lessonRepository.findAll();
        Collections.shuffle(all);
        return all.subList(0, Math.min(all.size(), 5));
    }

    public void submitReview(Long userId, Long lessonId, String rating) {
        // Mock logic: Calculate next review date based on rating (forgot, hard, easy)
        // Update LearningRecord
        System.out.println("User " + userId + " reviewed lesson " + lessonId + " as " + rating);
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
}
