package com.codelab.app.data;

public class CatalogEntry {
    public final String id;
    public final String title;
    public final String language;
    public final int lessonCount;
    public final String difficulty;
    public final int sizeKb;
    public final String description;
    public final boolean builtIn;
    public final String type; // "language" or "lesson"

    public CatalogEntry(String id, String title, String language, int lessonCount,
                        String difficulty, int sizeKb, String description, boolean builtIn) {
        this(id, title, language, lessonCount, difficulty, sizeKb, description, builtIn, "language");
    }

    public CatalogEntry(String id, String title, String language, int lessonCount,
                        String difficulty, int sizeKb, String description, boolean builtIn, String type) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.lessonCount = lessonCount;
        this.difficulty = difficulty;
        this.sizeKb = sizeKb;
        this.description = description;
        this.builtIn = builtIn;
        this.type = type;
    }
}
