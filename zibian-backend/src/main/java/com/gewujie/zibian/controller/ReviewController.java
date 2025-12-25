package com.gewujie.zibian.controller;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")

public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/list")
    public List<Lesson> getReviewList(@RequestParam Long userId) {
        return reviewService.getReviewList(userId);
    }

    @PostMapping("/submit")
    public void submitReview(@RequestParam Long userId, @RequestParam Long lessonId, @RequestParam String rating) {
        reviewService.submitReview(userId, lessonId, rating);
    }
}
