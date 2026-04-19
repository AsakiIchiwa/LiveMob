package com.codelab.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads lesson packs from app assets.
 *  - packs/catalog.json     → marketplace listing (one entry per pack)
 *  - packs/<id>.json        → full pack content (lessons)
 * Tracks which packs are installed via SharedPreferences. Built-in packs
 * (builtIn=true) are always installed and cannot be uninstalled.
 *
 * Designed so this repository can later be swapped for a Retrofit-backed
 * implementation when the backend gets pack endpoints — same public API.
 */
public final class PackRepository {
    private static final String PREFS = "codelab_packs";
    private static final String KEY_INSTALLED = "installed_packs";
    private static final String KEY_ACTIVE = "active_pack";

    private static PackRepository INSTANCE;
    private final Context app;
    private final SharedPreferences sp;
    private final Gson gson = new Gson();

    private List<CatalogEntry> catalog;

    private PackRepository(Context ctx) {
        app = ctx.getApplicationContext();
        sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized PackRepository get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new PackRepository(ctx);
        return INSTANCE;
    }

    /** Catalog = available packs (marketplace). Just metadata, no lessons. */
    public List<CatalogEntry> catalog() {
        if (catalog == null) {
            String json = readAsset("packs/catalog.json");
            try {
                JsonObject root = gson.fromJson(json, JsonObject.class);
                if (root != null && root.has("packs")) {
                    CatalogEntry[] arr = gson.fromJson(root.get("packs"), CatalogEntry[].class);
                    catalog = (arr == null) ? new ArrayList<>() : Arrays.asList(arr);
                } else {
                    catalog = new ArrayList<>();
                }
            } catch (Exception e) {
                catalog = new ArrayList<>();
            }
        }
        return catalog;
    }

    /** Fully loaded pack including lessons. Returns null if pack not in catalog. */
    public LessonPack loadPack(String id) {
        CatalogEntry entry = null;
        for (CatalogEntry c : catalog()) if (c.id.equals(id)) { entry = c; break; }
        if (entry == null) return null;
        String json = readAsset("packs/" + id + ".json");
        try {
            return gson.fromJson(json, LessonPack.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<String> installedIds() {
        Set<String> ids = new HashSet<>(sp.getStringSet(KEY_INSTALLED, new HashSet<>()));
        // Built-in packs are always installed
        for (CatalogEntry c : catalog()) if (c.builtIn) ids.add(c.id);
        return ids;
    }

    public boolean isInstalled(String packId) {
        return installedIds().contains(packId);
    }

    public void install(String packId) {
        Set<String> ids = new HashSet<>(sp.getStringSet(KEY_INSTALLED, new HashSet<>()));
        if (ids.add(packId)) sp.edit().putStringSet(KEY_INSTALLED, ids).apply();
    }

    public void uninstall(String packId) {
        for (CatalogEntry c : catalog()) {
            if (c.id.equals(packId) && c.builtIn) return; // can't uninstall built-in
        }
        Set<String> ids = new HashSet<>(sp.getStringSet(KEY_INSTALLED, new HashSet<>()));
        if (ids.remove(packId)) sp.edit().putStringSet(KEY_INSTALLED, ids).apply();
    }

    public List<CatalogEntry> installedEntries() {
        List<CatalogEntry> out = new ArrayList<>();
        Set<String> ids = installedIds();
        for (CatalogEntry c : catalog()) if (ids.contains(c.id)) out.add(c);
        return out;
    }

    public String activePackId() {
        String id = sp.getString(KEY_ACTIVE, null);
        if (id != null && isInstalled(id)) return id;
        List<CatalogEntry> installed = installedEntries();
        return installed.isEmpty() ? null : installed.get(0).id;
    }

    public void setActivePackId(String packId) {
        sp.edit().putString(KEY_ACTIVE, packId).apply();
    }

    public void clear() {
        sp.edit().clear().apply();
        catalog = null;
    }

    private String readAsset(String path) {
        AssetManager am = app.getAssets();
        try (InputStream is = am.open(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        } catch (IOException e) {
            return "{}";
        }
    }
}
