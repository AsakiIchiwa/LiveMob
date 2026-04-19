package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class CreateSessionResponse {
    @SerializedName("session_id") public String sessionId;
    public String status;
    public String language;
    @SerializedName("language_version") public String languageVersion;
    @SerializedName("expires_at") public String expiresAt;
    @SerializedName("created_at") public String createdAt;
}
