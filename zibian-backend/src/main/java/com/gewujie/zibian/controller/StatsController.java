package com.gewujie.zibian.controller;

import com.gewujie.zibian.service.LearningService;
import com.gewujie.zibian.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/api/stats")

public class StatsController {

    @Autowired
    private LearningService learningService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> stats = new HashMap<>();
        stats.put("dailyNewWords", learningService.getDailyNewWords(userId));
        stats.put("collectedWords", learningService.getLearnedCharacters(userId));
        stats.put("pendingReviewsCount", reviewService.getPendingReviewsCount(userId));
        return stats;
    }

    @GetMapping("/learning-dates")
    public Map<String, String> getLearningDates() {
        return learningService.getLearningDates(StpUtil.getLoginIdAsLong());
    }

    @PostMapping("/check-in")
    public Map<String, Boolean> checkIn() {
        boolean success = learningService.checkIn(StpUtil.getLoginIdAsLong());
        return Map.of("success", success, "alreadyCheckedIn", !success);
    }

    @GetMapping("/is-checked-in-today")
    public Map<String, Boolean> isCheckedInToday() {
        boolean isCheckedIn = learningService.isCheckedInToday(StpUtil.getLoginIdAsLong());
        return Map.of("isCheckedIn", isCheckedIn);
    }

    @GetMapping("/learning-trend")
    public List<Map<String, Object>> getLearningTrend() {
        return learningService.getLearningTrend(StpUtil.getLoginIdAsLong());
    }

    @GetMapping("/learned-records")
    public List<Map<String, Object>> getLearningRecords() {
        return learningService.getLearningRecords(StpUtil.getLoginIdAsLong());
    }

    @GetMapping("/today-game-words")
    public Map<String, Long> getTodayGameWordsCount() {
        return Map.of("count", learningService.getTodayGameWordsCount(StpUtil.getLoginIdAsLong()));
    }
}
