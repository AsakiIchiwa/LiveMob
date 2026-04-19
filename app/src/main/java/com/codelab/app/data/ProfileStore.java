package com.codelab.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public final class ProfileStore {
    private static final String KEY = "user_profile";
    private static ProfileStore INSTANCE;
    private final SharedPreferences sp;
    private final Gson gson = new Gson();
    private UserProfile cached;

    private ProfileStore(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences("codelab_profile", Context.MODE_PRIVATE);
    }

    public static synchronized ProfileStore get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new ProfileStore(ctx);
        return INSTANCE;
    }

    public UserProfile get() {
        if (cached != null) return cached;
        String json = sp.getString(KEY, null);
        cached = (json == null) ? UserProfile.createDefault() : gson.fromJson(json, UserProfile.class);
        if (cached == null) cached = UserProfile.createDefault();
        return cached;
    }

    public void save(UserProfile p) {
        cached = p;
        sp.edit().putString(KEY, gson.toJson(p)).apply();
    }

    public void update(Updater updater) {
        UserProfile p = get();
        updater.update(p);
        save(p);
    }

    public interface Updater { void update(UserProfile p); }

    public void clear() {
        sp.edit().clear().apply();
        cached = null;
    }

    /** Update level based on XP. Simple curve: every 500 XP = 1 level. */
    public static int levelFromXp(int xp) {
        return Math.max(1, xp / 500 + 1);
    }

    /** Award XP and recompute level + task counter. */
    public void awardXp(int amount, boolean completedTask) {
        update(p -> {
            p.xp += amount;
            if (completedTask) p.tasksCompleted += 1;
            p.level = levelFromXp(p.xp);
        });
    }

    /** Mark today as an active day; update streak. */
    public void touchActivity() {
        long today = System.currentTimeMillis() / 86_400_000L;
        update(p -> {
            if (p.lastActiveEpochDay == today) return;       // already counted
            if (p.lastActiveEpochDay == today - 1) p.streakDays += 1; // continued
            else p.streakDays = 1;                           // reset / start
            p.lastActiveEpochDay = today;
        });
    }
}
