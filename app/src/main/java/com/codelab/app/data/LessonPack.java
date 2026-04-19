package com.codelab.app.data;

import java.util.List;

public class LessonPack {
    public final String id;
    public final String title;
    public final String description;
    public final String language;
    public final String difficulty;
    public final int sizeKb;
    public final boolean builtIn;
    public final List<Lesson> lessons;

    public LessonPack(String id, String title, String description, String language,
                      String difficulty, int sizeKb, boolean builtIn, List<Lesson> lessons) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.language = language;
        this.difficulty = difficulty;
        this.sizeKb = sizeKb;
        this.builtIn = builtIn;
        this.lessons = lessons;
    }
}
