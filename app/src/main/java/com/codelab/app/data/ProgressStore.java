package com.codelab.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks lesson progress per pack. Keyed by "packId:lessonIndex".
 * States: completed (boolean), attempted (seen/ran at least once).
 */
public final class ProgressStore {
    private static final String PREFS = "codelab_progress";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_ATTEMPTED = "attempted";

    private static ProgressStore INSTANCE;
    private final SharedPreferences sp;

    private ProgressStore(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized ProgressStore get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new ProgressStore(ctx);
        return INSTANCE;
    }

    private String key(String packId, int index) { return packId + ":" + index; }

    public boolean isCompleted(String packId, int index) {
        return sp.getStringSet(KEY_COMPLETED, new HashSet<>()).contains(key(packId, index));
    }

    public boolean isAttempted(String packId, int index) {
        return sp.getStringSet(KEY_ATTEMPTED, new HashSet<>()).contains(key(packId, index));
    }

    public void markAttempted(String packId, int index) {
        Set<String> s = new HashSet<>(sp.getStringSet(KEY_ATTEMPTED, new HashSet<>()));
        if (s.add(key(packId, index))) sp.edit().putStringSet(KEY_ATTEMPTED, s).apply();
    }

    public void markCompleted(String packId, int index) {
        Set<String> s = new HashSet<>(sp.getStringSet(KEY_COMPLETED, new HashSet<>()));
        if (s.add(key(packId, index))) {
            sp.edit().putStringSet(KEY_COMPLETED, s).apply();
            // Also mark attempted
            markAttempted(packId, index);
        }
    }

    public Lesson.Status statusFor(String packId, int index) {
        if (isCompleted(packId, index)) return Lesson.Status.COMPLETED;
        if (isAttempted(packId, index)) return Lesson.Status.IN_PROGRESS;
        return Lesson.Status.NOT_STARTED;
    }

    public int countCompleted(String packId, int total) {
        int n = 0;
        for (int i = 1; i <= total; i++) if (isCompleted(packId, i)) n++;
        return n;
    }

    public void reset() { sp.edit().clear().apply(); }
}
