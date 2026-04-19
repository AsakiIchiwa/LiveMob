package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateSessionResponse {
    @SerializedName("session_id") public String sessionId;
    public String status;
    public int version;
    @SerializedName("updated_at") public String updatedAt;
}
