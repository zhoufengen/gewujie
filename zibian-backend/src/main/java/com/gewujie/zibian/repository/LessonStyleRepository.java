package com.gewujie.zibian.repository;

import com.gewujie.zibian.model.LessonStyle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonStyleRepository extends JpaRepository<LessonStyle, Long> {
    void deleteByLessonId(Long lessonId);
}
