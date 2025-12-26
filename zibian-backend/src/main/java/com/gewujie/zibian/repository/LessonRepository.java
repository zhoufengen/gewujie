package com.gewujie.zibian.repository;

import com.gewujie.zibian.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByCharacter(String character);
    List<Lesson> findByTextbookCategory(String textbookCategory);
    List<Lesson> findByTextbookCategoryOrderByCharacter(String textbookCategory);
    boolean existsByCharacter(String character);
}
