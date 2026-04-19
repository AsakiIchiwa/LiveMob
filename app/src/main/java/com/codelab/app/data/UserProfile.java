package com.codelab.app.data;

public class UserProfile {
    public String name;
    public String handle;
    public String bio;
    public int avatarVariant;     // 0..N — picks an avatar tint
    public int xp;
    public int level;
    public int tasksCompleted;
    public int streakDays;
    public long lastActiveEpochDay; // System.currentTimeMillis() / 86_400_000L

    public UserProfile() {}

    public UserProfile(String name, String handle, String bio, int avatarVariant) {
        this.name = name;
        this.handle = handle;
        this.bio = bio;
        this.avatarVariant = avatarVariant;
        this.xp = 0;
        this.level = 1;
        this.tasksCompleted = 0;
        this.streakDays = 0;
        this.lastActiveEpochDay = 0;
    }

    public static UserProfile createDefault() {
        UserProfile p = new UserProfile("Coder", "@coder", "Learning to code on mobile.", 0);
        return p;
    }
}
