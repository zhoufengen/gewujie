package com.gewujie.zibian.controller;

import com.gewujie.zibian.service.LearningService;
import com.gewujie.zibian.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
}
