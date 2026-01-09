package com.gewujie.zibian.controller;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/api/review")

public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/list")
    public List<Lesson> getReviewList(@RequestParam(required = false) String textbookCategory) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (textbookCategory != null) {
            return reviewService.getReviewListByTextbookCategory(userId, textbookCategory);
        }
        return reviewService.getReviewList(userId);
    }

    @PostMapping("/submit")
    public void submitReview(@RequestParam Long lessonId, @RequestParam String rating) {
        reviewService.submitReview(StpUtil.getLoginIdAsLong(), lessonId, rating);
    }
}
