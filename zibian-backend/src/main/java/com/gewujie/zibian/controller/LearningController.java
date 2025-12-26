package com.gewujie.zibian.controller;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")

public class LearningController {

    @Autowired
    private LearningService learningService;

    @GetMapping("/lesson/current")
    public Lesson getCurrentLesson() {
        // For MVP, just return a random lesson
        return learningService.getRandomLesson();
    }

    @GetMapping("/lesson/current/{textbookCategory}")
    public Lesson getCurrentLessonByTextbook(@PathVariable String textbookCategory, @RequestParam(required = false) Long userId) {
        // Return a random lesson from the specified textbook category, filtered by user's learning history if userId is provided
        return learningService.getRandomLessonByTextbookCategory(textbookCategory, userId);
    }

    @GetMapping("/lesson/{id}")
    public Lesson getLesson(@PathVariable Long id) {
        return learningService.getLesson(id);
    }

    @GetMapping("/lessons/{textbookCategory}")
    public List<Lesson> getLessonsByTextbookCategory(@PathVariable String textbookCategory) {
        return learningService.getLessonsByTextbookCategory(textbookCategory);
    }

    @PostMapping("/record")
    public void recordLearning(@RequestParam Long userId, @RequestParam Long lessonId) {
        learningService.recordLearning(userId, lessonId);
    }
}
