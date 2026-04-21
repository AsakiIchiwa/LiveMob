package com.codelab.app.data;

import java.util.ArrayList;
import java.util.List;

public class RecentSession {
    public final String id;            // local UUID
    public final String filename;      // display name (primary file)
    public final String where;         // "Playground" or "Lesson 4"
    public final String language;      // java / python / etc
    public final String sourceCode;    // last saved code (primary file, kept for compat)
    public final long timestampMs;
    public final String backendSessionId; // optional — server-side session id

    /** All files in this project. If null/empty, fall back to single-file (filename + sourceCode). */
    public List<ProjectFile> files;

    public RecentSession(String id, String filename, String where, String language,
                         String sourceCode, long timestampMs, String backendSessionId) {
        this.id = id;
        this.filename = filename;
        this.where = where;
        this.language = language;
        this.sourceCode = sourceCode;
        this.timestampMs = timestampMs;
        this.backendSessionId = backendSessionId;
    }

    /** Returns all project files; if none stored, synthesizes from single-file fields. */
    public List<ProjectFile> getFiles() {
        if (files != null && !files.isEmpty()) return files;
        List<ProjectFile> list = new ArrayList<>();
        list.add(new ProjectFile(filename, sourceCode != null ? sourceCode : ""));
        return list;
    }

    /** Simple inner class representing one file in a project. */
    public static class ProjectFile {
        public String name;
        public String content;

        public ProjectFile() {}

        public ProjectFile(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }
}
