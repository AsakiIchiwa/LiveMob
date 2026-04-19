package com.codelab.app.api.dto;

import com.google.gson.annotations.SerializedName;

public class DeviceLoginRequest {
    @SerializedName("device_id") public String deviceId;

    public DeviceLoginRequest(String deviceId) {
        this.deviceId = deviceId;
    }
}
