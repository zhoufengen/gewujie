package com.gewujie.zibian.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1)
    private String character;

    private String pinyin;

    @Column(columnDefinition = "TEXT")
    private String definition;

    // Comma-separated example words using this character
    private String words;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonStyle> styles;
}
