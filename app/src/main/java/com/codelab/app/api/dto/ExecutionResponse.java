package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class ExecutionResponse {
    @SerializedName("execution_id") public String executionId;
    @SerializedName("session_id")   public String sessionId;
    public String status;
    public String stdout;
    public String stderr;
    @SerializedName("exit_code")        public Integer exitCode;
    @SerializedName("execution_time_ms") public Integer executionTimeMs;
    @SerializedName("memory_used_kb")    public Integer memoryUsedKb;
}
