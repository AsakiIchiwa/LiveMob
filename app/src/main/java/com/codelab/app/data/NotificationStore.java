package com.codelab.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persists in-app notifications in SharedPreferences as JSON.
 */
public final class NotificationStore {
    private static final String PREFS = "notifications";
    private static final String KEY_LIST = "list";
    private static NotificationStore INSTANCE;

    private final SharedPreferences sp;
    private final Gson gson = new Gson();

    private NotificationStore(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized NotificationStore get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new NotificationStore(ctx);
        return INSTANCE;
    }

    public List<AppNotification> all() {
        String json = sp.getString(KEY_LIST, null);
        if (json == null) return new ArrayList<>();
        Type t = new TypeToken<List<AppNotification>>() {}.getType();
        List<AppNotification> list = gson.fromJson(json, t);
        if (list == null) return new ArrayList<>();
        // Sort newest first
        Collections.sort(list, (a, b) -> Long.compare(b.timestamp, a.timestamp));
        return list;
    }

    public void add(AppNotification n) {
        List<AppNotification> list = all();
        list.add(0, n);
        // Keep max 50
        if (list.size() > 50) list = list.subList(0, 50);
        sp.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }

    public int unreadCount() {
        int count = 0;
        for (AppNotification n : all()) {
            if (!n.read) count++;
        }
        return count;
    }

    public void markAllRead() {
        List<AppNotification> list = all();
        for (AppNotification n : list) n.read = true;
        sp.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }

    public void markRead(String id) {
        List<AppNotification> list = all();
        for (AppNotification n : list) {
            if (n.id.equals(id)) { n.read = true; break; }
        }
        sp.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }

    public void clear() {
        sp.edit().remove(KEY_LIST).apply();
    }
}
