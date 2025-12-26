package com.gewujie.zibian.controller;

import com.gewujie.zibian.service.LearningService;
import com.gewujie.zibian.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")

public class StatsController {

    @Autowired
    private LearningService learningService;
    
    @Autowired
    private ReviewService reviewService;

    @GetMapping("/summary")
    public Map<String, Object> getSummary(@RequestParam Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("dailyNewWords", learningService.getDailyNewWords(userId));
        stats.put("collectedWords", learningService.getLearnedCharacters(userId));
        stats.put("pendingReviewsCount", reviewService.getPendingReviewsCount(userId));
        return stats;
    }
    
    @GetMapping("/learning-dates")
    public Map<String, String> getLearningDates(@RequestParam Long userId) {
        return learningService.getLearningDates(userId);
    }
    
    @PostMapping("/check-in")
    public Map<String, Boolean> checkIn(@RequestParam Long userId) {
        boolean success = learningService.checkIn(userId);
        return Map.of("success", success, "alreadyCheckedIn", !success);
    }
    
    @GetMapping("/is-checked-in-today")
    public Map<String, Boolean> isCheckedInToday(@RequestParam Long userId) {
        boolean isCheckedIn = learningService.isCheckedInToday(userId);
        return Map.of("isCheckedIn", isCheckedIn);
    }
    
    @GetMapping("/learning-trend")
    public List<Map<String, Object>> getLearningTrend(@RequestParam Long userId) {
        return learningService.getLearningTrend(userId);
    }
}
