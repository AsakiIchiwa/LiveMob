package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateSessionRequest {
    @SerializedName("source_code") public String code;
    public int version;

    public UpdateSessionRequest(String code, int version) {
        this.code = code;
        this.version = version;
    }
}
