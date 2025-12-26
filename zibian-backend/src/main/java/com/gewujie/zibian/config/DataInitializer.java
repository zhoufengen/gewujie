package com.gewujie.zibian.config;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.model.LessonStyle;
import com.gewujie.zibian.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private LearningService learningService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Force re-seed if data is missing or incomplete
        if (!learningService.isDataReady()) {
            System.out.println("Data missing or incomplete. Seeding database...");
            try {
                seedData();
            } catch (Exception e) {
                System.err.println("Error seeding database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void seedData() {
        System.out.println("Seeding database with initial data...");

        // Create initial lesson with '永' character
        Lesson lesson = new Lesson();
        lesson.setCharacter("永");
        lesson.setPinyin("yǒng");
        lesson.setDefinition("Water flowing long and deep; eternal, forever, always.");
        lesson.setWords("永远,永久,永恒");
        lesson.setTextbookCategory("启蒙词本"); // Set textbook category

        List<LessonStyle> styles = new ArrayList<>();
        styles.add(createStyle("甲骨文", "https://via.placeholder.com/150?text=Oracle"));
        styles.add(createStyle("金文", "https://via.placeholder.com/150?text=Bronze"));
        styles.add(createStyle("小篆", "https://via.placeholder.com/150?text=Seal"));
        styles.add(createStyle("隶书", "https://via.placeholder.com/150?text=Clerical"));
        styles.add(createStyle("楷书", "https://via.placeholder.com/150?text=Regular"));
        styles.add(createStyle("草书", "https://via.placeholder.com/150?text=Cursive"));
        styles.add(createStyle("行书", "https://via.placeholder.com/150?text=Running"));

        lesson.setStyles(styles);

        // This will create the lesson or update it if it already exists due to JPA's save() behavior
        learningService.createLesson(lesson);
        System.out.println("Lesson created/updated!");

        // Import characters from textbooks
        System.out.println("Importing characters from textbooks...");
        String projectPath = System.getProperty("user.dir");
        String docsPath = projectPath.replace("zibian-backend", "docs/ciben/");
        
        try {
            // Import 启蒙词本
            File qimengFile = new File(docsPath + "启蒙词本300.txt");
            if (qimengFile.exists()) {
                System.out.println("Importing 启蒙词本300.txt...");
                learningService.importCharactersFromFile(qimengFile.getAbsolutePath(), "启蒙词本");
                System.out.println("Imported 启蒙词本300.txt");
            } else {
                System.out.println("File not found: " + qimengFile.getAbsolutePath());
            }
            
            // Import 小学词本
            File xiaoxueFile = new File(docsPath + "小学词本1000.txt");
            if (xiaoxueFile.exists()) {
                System.out.println("Importing 小学词本1000.txt...");
                learningService.importCharactersFromFile(xiaoxueFile.getAbsolutePath(), "小学词本");
                System.out.println("Imported 小学词本1000.txt");
            } else {
                System.out.println("File not found: " + xiaoxueFile.getAbsolutePath());
            }
            
            // Import 中学词本
            File zhongxueFile = new File(docsPath + "中学词本3500.txt");
            if (zhongxueFile.exists()) {
                System.out.println("Importing 中学词本3500.txt...");
                learningService.importCharactersFromFile(zhongxueFile.getAbsolutePath(), "中学词本");
                System.out.println("Imported 中学词本3500.txt");
            } else {
                System.out.println("File not found: " + zhongxueFile.getAbsolutePath());
            }
            
            // Update all lessons with character data (pinyin, definition, words)
            learningService.updateAllLessonsCharacterData();
            
        } catch (Exception e) {
            System.out.println("Error importing characters from textbooks: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Database seeded!");
    }

    private LessonStyle createStyle(String name, String img) {
        LessonStyle style = new LessonStyle();
        style.setName(name);
        style.setImgUrl(img);
        return style;
    }
}
