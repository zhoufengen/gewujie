package com.gewujie.zibian.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "lessons")
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
    
    // Textbook category: 启蒙词本, 小学词本, 中学词本
    @Column
    private String textbookCategory;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonStyle> styles;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getTextbookCategory() {
        return textbookCategory;
    }

    public void setTextbookCategory(String textbookCategory) {
        this.textbookCategory = textbookCategory;
    }

    public List<LessonStyle> getStyles() {
        return styles;
    }

    public void setStyles(List<LessonStyle> styles) {
        this.styles = styles;
    }
}
