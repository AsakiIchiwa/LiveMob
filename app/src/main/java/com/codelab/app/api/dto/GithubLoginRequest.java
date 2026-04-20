package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class GithubLoginRequest {
    public String code;
    @SerializedName("redirect_uri") public String redirectUri;

    public GithubLoginRequest(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
    }
}
