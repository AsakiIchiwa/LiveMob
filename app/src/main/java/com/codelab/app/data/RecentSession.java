package com.codelab.app.data;

public class RecentSession {
    public final String id;            // local UUID
    public final String filename;      // display name
    public final String where;         // "Playground" or "Lesson 4"
    public final String language;      // java / python / etc
    public final String sourceCode;    // last saved code
    public final long timestampMs;
    public final String backendSessionId; // optional — server-side session id

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
}
