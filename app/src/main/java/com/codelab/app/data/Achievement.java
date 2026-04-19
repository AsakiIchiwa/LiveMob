package com.codelab.app.data;

public class Achievement {
    public final String id;
    public final String title;
    public final String description;
    public final int iconRes;
    public final boolean unlocked;

    public Achievement(String id, String title, String description, int iconRes, boolean unlocked) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.unlocked = unlocked;
    }
}
