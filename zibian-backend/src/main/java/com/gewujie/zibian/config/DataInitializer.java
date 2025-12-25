package com.gewujie.zibian.config;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.model.LessonStyle;
import com.gewujie.zibian.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private LearningService learningService;

    @Override
    public void run(String... args) throws Exception {
        // Force re-seed if data is missing or incomplete
        if (!learningService.isDataReady()) {
            System.out.println("Data missing or incomplete. Seeding database...");
            seedData();
        }
    }

    private void seedData() {
        System.out.println("Seeding database with initial data...");

        // Try to update existing lesson first
        Lesson existing = learningService.findByCharacter("永");
        if (existing != null) {
            System.out.println("Updating existing lesson with words...");
            existing.setWords("永远,永久,永恒");
            learningService.updateLesson(existing);
            System.out.println("Lesson updated!");
            return;
        }

        // Create new lesson if not exists
        Lesson lesson = new Lesson();
        lesson.setCharacter("永");
        lesson.setPinyin("yǒng");
        lesson.setDefinition("Water flowing long and deep; eternal, forever, always.");
        lesson.setWords("永远,永久,永恒");

        List<LessonStyle> styles = new ArrayList<>();
        styles.add(createStyle("甲骨文", "https://via.placeholder.com/150?text=Oracle"));
        styles.add(createStyle("金文", "https://via.placeholder.com/150?text=Bronze"));
        styles.add(createStyle("小篆", "https://via.placeholder.com/150?text=Seal"));
        styles.add(createStyle("隶书", "https://via.placeholder.com/150?text=Clerical"));
        styles.add(createStyle("楷书", "https://via.placeholder.com/150?text=Regular"));
        styles.add(createStyle("草书", "https://via.placeholder.com/150?text=Cursive"));
        styles.add(createStyle("行书", "https://via.placeholder.com/150?text=Running"));

        lesson.setStyles(styles);

        learningService.createLesson(lesson);
        System.out.println("Database seeded!");
    }

    private LessonStyle createStyle(String name, String img) {
        LessonStyle style = new LessonStyle();
        style.setName(name);
        style.setImgUrl(img);
        return style;
    }
}
