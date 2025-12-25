package com.gewujie.zibian.controller;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/lesson/{id}")
    public Lesson getLesson(@PathVariable Long id) {
        return learningService.getLesson(id);
    }

    @PostMapping("/record")
    public void recordLearning(@RequestParam Long userId, @RequestParam Long lessonId) {
        learningService.recordLearning(userId, lessonId);
    }
}
