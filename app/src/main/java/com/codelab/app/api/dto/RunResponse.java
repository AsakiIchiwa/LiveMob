package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class RunResponse {
    @SerializedName("execution_id") public String executionId;
    public String status;
    public String message;
}
