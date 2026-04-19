package com.codelab.app;

import android.app.Application;

import com.codelab.app.data.SettingsStore;

public class CodeLabApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Apply persisted theme as early as possible
        SettingsStore.applyTheme(SettingsStore.get(this).theme());
    }
}
