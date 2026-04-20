package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class CreateSessionRequest {
    @SerializedName("simulation_id") public String simulationId;
    @SerializedName("user_id")       public String userId;
    public String language;
    @SerializedName("template_code") public String templateCode;

    public CreateSessionRequest(String simulationId, String userId, String language, String code) {
        this.simulationId = simulationId;
        this.userId = userId;
        this.language = language;
        this.templateCode = code;
    }
}
