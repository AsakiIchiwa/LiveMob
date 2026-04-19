package com.codelab.app.ui.settings;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.codelab.app.R;
import com.codelab.app.data.SettingsStore;

public class SettingsActivity extends AppCompatActivity {

    private static final String[] THEMES = { SettingsStore.THEME_DARK, SettingsStore.THEME_LIGHT, SettingsStore.THEME_SYSTEM };
    private static final String[] THEME_LABELS = { "Dark", "Light", "Follow system" };

    private static final String[] CODE_LANGS = { "java", "python", "javascript", "typescript", "cpp" };
    private static final String[] CODE_LANG_LABELS = { "Java", "Python", "JavaScript", "TypeScript", "C++" };

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

        // Default language
        ((TextView) findViewById(R.id.rowDefaultLangValue)).setText(labelFor(CODE_LANGS, CODE_LANG_LABELS, s.defaultCodeLang()));
        findViewById(R.id.rowDefaultLang).setOnClickListener(v ->
                pickFromList("Default Language", CODE_LANG_LABELS, indexOf(CODE_LANGS, s.defaultCodeLang()), idx -> {
                    s.setDefaultCodeLang(CODE_LANGS[idx]);
                    ((TextView) findViewById(R.id.rowDefaultLangValue)).setText(CODE_LANG_LABELS[idx]);
                }));

        // Notifications
        Switch switchNotifications = findViewById(R.id.switchNotifications);
        switchNotifications.setChecked(s.notifications());
        switchNotifications.setOnCheckedChangeListener((btn, checked) -> s.setNotifications(checked));
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
