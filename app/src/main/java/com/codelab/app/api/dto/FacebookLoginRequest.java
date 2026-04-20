package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class FacebookLoginRequest {
    @SerializedName("access_token") public String accessToken;

    public FacebookLoginRequest(String accessToken) {
        this.accessToken = accessToken;
    }
}
