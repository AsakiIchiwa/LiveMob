package com.codelab.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Persists the last N coding sessions (Playground or Lesson) so they show up
 * on the Home screen and can be reopened.
 */
public final class RecentSessionStore {
    private static final String PREFS = "codelab_sessions";
    private static final String KEY = "recent_sessions";
    private static final int MAX = 20;

    private static RecentSessionStore INSTANCE;
    private final SharedPreferences sp;
    private final Gson gson = new Gson();

    private RecentSessionStore(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized RecentSessionStore get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new RecentSessionStore(ctx);
        return INSTANCE;
    }

    public List<RecentSession> all() {
        String json = sp.getString(KEY, null);
        if (json == null) return new ArrayList<>();
        try {
            List<RecentSession> list = gson.fromJson(json,
                    new TypeToken<List<RecentSession>>(){}.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Save or update a session by its id (stable across runs of the same logical session).
     * If id is null, creates a new one. Returns the id used.
     */
    public String save(String id, String filename, String where, String language,
                       String sourceCode, String backendSessionId) {
        List<RecentSession> list = all();
        // Remove existing entry with same id so the new one bubbles to the top
        if (id != null) {
            for (Iterator<RecentSession> it = list.iterator(); it.hasNext(); ) {
                if (id.equals(it.next().id)) { it.remove(); break; }
            }
        } else {
            id = UUID.randomUUID().toString();
        }
        RecentSession s = new RecentSession(id, filename, where, language,
                sourceCode, System.currentTimeMillis(), backendSessionId);
        list.add(0, s);
        while (list.size() > MAX) list.remove(list.size() - 1);
        sp.edit().putString(KEY, gson.toJson(list)).apply();
        return id;
    }

    public RecentSession findById(String id) {
        for (RecentSession s : all()) if (s.id.equals(id)) return s;
        return null;
    }

    public void clear() {
        sp.edit().clear().apply();
    }
}
