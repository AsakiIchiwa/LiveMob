package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class RunRequest {
    @SerializedName("user_id") public String userId;
    public RunRequest(String userId) { this.userId = userId; }
}
