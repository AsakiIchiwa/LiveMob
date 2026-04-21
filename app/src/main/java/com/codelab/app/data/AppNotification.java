package com.codelab.app.data;

/**
 * A single in-app notification entry.
 */
public class AppNotification {
    public enum Type { ACHIEVEMENT, LESSON, SESSION, SYSTEM }

    public String id;
    public Type type;
    public String title;
    public String body;
    public long timestamp;  // System.currentTimeMillis()
    public boolean read;

    public AppNotification() {}

    public AppNotification(Type type, String title, String body) {
        this.id = java.util.UUID.randomUUID().toString();
        this.type = type;
        this.title = title;
        this.body = body;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }
}
