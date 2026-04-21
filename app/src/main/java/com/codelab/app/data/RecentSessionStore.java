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

    /** Save the file list for a session. */
    public void saveFiles(String id, List<RecentSession.ProjectFile> files) {
        List<RecentSession> list = all();
        for (int i = 0; i < list.size(); i++) {
            RecentSession s = list.get(i);
            if (id.equals(s.id)) {
                s.files = new ArrayList<>(files);
                list.set(i, s);
                break;
            }
        }
        sp.edit().putString(KEY, gson.toJson(list)).apply();
    }

    /** Delete a session by id. */
    public void delete(String id) {
        List<RecentSession> list = all();
        for (Iterator<RecentSession> it = list.iterator(); it.hasNext(); ) {
            if (id.equals(it.next().id)) { it.remove(); break; }
        }
        sp.edit().putString(KEY, gson.toJson(list)).apply();
    }

    /** Rename a session's display filename. */
    public void rename(String id, String newFilename) {
        List<RecentSession> list = all();
        for (int i = 0; i < list.size(); i++) {
            RecentSession s = list.get(i);
            if (id.equals(s.id)) {
                RecentSession updated = new RecentSession(s.id, newFilename, s.where,
                        s.language, s.sourceCode, s.timestampMs, s.backendSessionId);
                updated.files = s.files;
                list.set(i, updated);
                break;
            }
        }
        sp.edit().putString(KEY, gson.toJson(list)).apply();
    }

    public void clear() {
        sp.edit().clear().apply();
    }
}
