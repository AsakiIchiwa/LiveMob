package com.codelab.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public final class Prefs {
    private static final String NAME = "codelab";

    private Prefs() {}

    public static SharedPreferences get(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static String getOrCreateUuid(Context ctx, String key) {
        SharedPreferences sp = get(ctx);
        String v = sp.getString(key, null);
        if (v == null) {
            v = UUID.randomUUID().toString();
            sp.edit().putString(key, v).apply();
        }
        return v;
    }

    public static String getString(Context ctx, String key, String def) {
        return get(ctx).getString(key, def);
    }

    public static void putString(Context ctx, String key, String val) {
        get(ctx).edit().putString(key, val).apply();
    }
}
