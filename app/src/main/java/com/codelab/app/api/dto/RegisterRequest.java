package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    public String email;
    public String password;
    @SerializedName("display_name") public String displayName;

    public RegisterRequest(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }
}
