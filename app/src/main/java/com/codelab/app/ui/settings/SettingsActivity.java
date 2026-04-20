package com.codelab.app.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.codelab.app.R;
import com.codelab.app.data.AuthManager;
import com.codelab.app.data.SettingsStore;
import com.codelab.app.ui.auth.LoginActivity;

import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private static final String[] THEMES = { SettingsStore.THEME_DARK, SettingsStore.THEME_LIGHT, SettingsStore.THEME_SYSTEM };
    private static final String[] THEME_LABELS = { "Dark", "Light", "Follow system" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.settingsBack).setOnClickListener(v -> finish());

        SettingsStore s = SettingsStore.get(this);

        // Font size +/-
        TextView fontSizeValue = findViewById(R.id.fontSizeValue);
        fontSizeValue.setText(String.valueOf(s.fontSize()));
        findViewById(R.id.btnFontMinus).setOnClickListener(v -> {
            int size = Math.max(10, s.fontSize() - 1);
            s.setFontSize(size);
            fontSizeValue.setText(String.valueOf(size));
        });
        findViewById(R.id.btnFontPlus).setOnClickListener(v -> {
            int size = Math.min(24, s.fontSize() + 1);
            s.setFontSize(size);
            fontSizeValue.setText(String.valueOf(size));
        });

        // Toggles
        Switch switchAutoSave = findViewById(R.id.switchAutoSave);
        switchAutoSave.setChecked(s.autoSave());
        switchAutoSave.setOnCheckedChangeListener((btn, checked) -> s.setAutoSave(checked));

        Switch switchLineNumbers = findViewById(R.id.switchLineNumbers);
        switchLineNumbers.setChecked(s.lineNumbers());
        switchLineNumbers.setOnCheckedChangeListener((btn, checked) -> s.setLineNumbers(checked));

        Switch switchWordWrap = findViewById(R.id.switchWordWrap);
        switchWordWrap.setChecked(s.wordWrap());
        switchWordWrap.setOnCheckedChangeListener((btn, checked) -> s.setWordWrap(checked));

        // Theme
        ((TextView) findViewById(R.id.rowThemeValue)).setText(labelFor(THEMES, THEME_LABELS, s.theme()));
        findViewById(R.id.rowTheme).setOnClickListener(v ->
                pickFromList("Theme", THEME_LABELS, indexOf(THEMES, s.theme()), idx -> {
                    s.setTheme(THEMES[idx]);
                    ((TextView) findViewById(R.id.rowThemeValue)).setText(THEME_LABELS[idx]);
                    SettingsStore.applyTheme(THEMES[idx]);
                }));

        // Notifications
        Switch switchNotifications = findViewById(R.id.switchNotifications);
        switchNotifications.setChecked(s.notifications());
        switchNotifications.setOnCheckedChangeListener((btn, checked) -> s.setNotifications(checked));

        // Accent color picker
        wireAccentColor(s);

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void wireAccentColor(SettingsStore s) {
        View[] swatches = new View[] {
                findViewById(R.id.colorBlue),
                findViewById(R.id.colorPurple),
                findViewById(R.id.colorGreen),
                findViewById(R.id.colorOrange),
                findViewById(R.id.colorRed),
        };
        String[] keys = new String[] {
                SettingsStore.ACCENT_BLUE, SettingsStore.ACCENT_PURPLE,
                SettingsStore.ACCENT_GREEN, SettingsStore.ACCENT_ORANGE,
                SettingsStore.ACCENT_RED,
        };
        Runnable refresh = () -> {
            String current = s.accentColor();
            for (int i = 0; i < swatches.length; i++) {
                swatches[i].setBackgroundResource(keys[i].equals(current)
                        ? R.drawable.bg_circle_accent
                        : R.drawable.bg_circle);
            }
        };
        refresh.run();
        for (int i = 0; i < swatches.length; i++) {
            final int idx = i;
            swatches[i].setOnClickListener(v -> {
                s.setAccentColor(keys[idx]);
                refresh.run();
            });
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setMessage("You'll need to sign in again to access your lessons and sessions.")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Log out", (d, w) -> doLogout())
                .show();
    }

    private void doLogout() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AuthManager.logout(this);
            runOnUiThread(() -> {
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            });
        });
    }

    private String labelFor(String[] keys, String[] labels, String key) {
        int i = indexOf(keys, key);
        return i < 0 ? labels[0] : labels[i];
    }

    private int indexOf(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(v)) return i;
        return 0;
    }

    private interface OnPicked { void onPicked(int index); }

    private void pickFromList(String title, String[] labels, int currentIndex, OnPicked picked) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setSingleChoiceItems(labels, currentIndex, (dlg, which) -> {
                    picked.onPicked(which);
                    dlg.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
