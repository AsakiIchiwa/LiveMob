package com.codelab.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class SettingsStore {
    private static final String PREFS = "codelab_settings";
    private static final String KEY_THEME = "theme";          // dark/light/system
    private static final String KEY_UI_LANG = "ui_lang";       // en, es, …
    private static final String KEY_DEFAULT_LANG = "default_code_lang"; // java, python, …
    private static final String KEY_BACKEND_URL = "backend_url";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_SYSTEM = "system";

    private static SettingsStore INSTANCE;
    private final SharedPreferences sp;

    private SettingsStore(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized SettingsStore get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new SettingsStore(ctx);
        return INSTANCE;
    }

    public String theme() { return sp.getString(KEY_THEME, THEME_DARK); }
    public void setTheme(String t) {
        sp.edit().putString(KEY_THEME, t).apply();
        applyTheme(t);
    }
    public static void applyTheme(String t) {
        switch (t) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case THEME_DARK:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    public String uiLang() { return sp.getString(KEY_UI_LANG, "en"); }
    public void setUiLang(String l) { sp.edit().putString(KEY_UI_LANG, l).apply(); }

    public String defaultCodeLang() { return sp.getString(KEY_DEFAULT_LANG, "java"); }
    public void setDefaultCodeLang(String l) { sp.edit().putString(KEY_DEFAULT_LANG, l).apply(); }

    public String backendUrl() {
        return sp.getString(KEY_BACKEND_URL, "https://live-code-execution-api.onrender.com/");
    }
    public void setBackendUrl(String url) {
        if (url != null && !url.endsWith("/")) url = url + "/";
        sp.edit().putString(KEY_BACKEND_URL, url).apply();
    }

    public String accessToken() { return sp.getString(KEY_ACCESS_TOKEN, null); }
    public void setAccessToken(String token) { sp.edit().putString(KEY_ACCESS_TOKEN, token).apply(); }
    public String refreshToken() { return sp.getString(KEY_REFRESH_TOKEN, null); }
    public void setRefreshToken(String token) { sp.edit().putString(KEY_REFRESH_TOKEN, token).apply(); }

    // Editor settings
    public int fontSize() { return sp.getInt("font_size", 14); }
    public void setFontSize(int s) { sp.edit().putInt("font_size", s).apply(); }

    public boolean autoSave() { return sp.getBoolean("auto_save", true); }
    public void setAutoSave(boolean v) { sp.edit().putBoolean("auto_save", v).apply(); }

    public boolean lineNumbers() { return sp.getBoolean("line_numbers", true); }
    public void setLineNumbers(boolean v) { sp.edit().putBoolean("line_numbers", v).apply(); }

    public boolean wordWrap() { return sp.getBoolean("word_wrap", false); }
    public void setWordWrap(boolean v) { sp.edit().putBoolean("word_wrap", v).apply(); }

    public boolean notifications() { return sp.getBoolean("notifications", true); }
    public void setNotifications(boolean v) { sp.edit().putBoolean("notifications", v).apply(); }

    public void clear() { sp.edit().clear().apply(); }
}
