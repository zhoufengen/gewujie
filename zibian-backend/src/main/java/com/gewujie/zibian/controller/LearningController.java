package com.gewujie.zibian.controller;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.model.User;
import com.gewujie.zibian.service.LearningService;
import com.gewujie.zibian.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    @Autowired
    private LearningService learningService;

    @Autowired
    private UserService userService;

    @GetMapping("/lesson/current")
    public Lesson getCurrentLesson(@RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "false") boolean isGame) {
        // For MVP, just return a random lesson, filtered by user's learning history if
        // userId is provided
        return learningService.getRandomLesson(userId, isGame);
    }

    @GetMapping("/lesson/current/{textbookCategory}")
    public Lesson getCurrentLessonByTextbook(@PathVariable String textbookCategory,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "false") boolean isGame) {
        // Return a random lesson from the specified textbook category, filtered by
        // user's learning history if userId is provided
        return learningService.getRandomLessonByTextbookCategory(textbookCategory, userId, isGame);
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
    public void recordLearning(@RequestParam Long userId, @RequestParam Long lessonId,
            @RequestParam(required = false, defaultValue = "false") boolean isGame) {
        learningService.recordLearning(userId, lessonId, isGame);
    }

    @GetMapping("/records")
    public List<Map<String, Object>> getLearningRecords(@RequestParam Long userId) {
        return learningService.getLearningRecords(userId);
    }

    @GetMapping("/daily-count")
    public long getDailyNewWords(@RequestParam Long userId) {
        return learningService.getDailyNewWords(userId);
    }

    @GetMapping("/user-info")
    public User getUserInfo(@RequestParam Long userId) {
        return userService.getUser(userId);
    }
}
