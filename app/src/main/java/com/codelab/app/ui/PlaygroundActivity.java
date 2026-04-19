package com.codelab.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.codelab.app.R;
import com.codelab.app.api.dto.ExecutionResponse;
import com.codelab.app.data.ProfileStore;
import com.codelab.app.data.RecentSession;
import com.codelab.app.data.RecentSessionStore;
import com.codelab.app.data.SettingsStore;
import com.codelab.app.util.CodeRunner;

public class PlaygroundActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_ID = "session_id";

    private static final String DEFAULT_CODE_JAVA =
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello, World!\");\n" +
            "    }\n" +
            "}\n";
    private static final String DEFAULT_CODE_PY =
            "print(\"Hello, World!\")\n";
    private static final String DEFAULT_CODE_JS =
            "console.log(\"Hello, World!\");\n";

    private EditText editor;
    private TextView lineNumbers, outputView, statusChip, statusLang, statusLine, fileTabLabel;
    private View btnRun;
    private View consolePanel;
    private DrawerLayout drawerLayout;
    private View explorerDrawer;
    private CodeRunner runner;
    private String currentLanguage;
    private String currentSessionId;
    private String currentFilename;
    private boolean awardedFirstRun;
    private boolean consoleVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);

        editor = findViewById(R.id.codeEditor);
        lineNumbers = findViewById(R.id.lineNumbers);
        outputView = findViewById(R.id.outputView);
        statusChip = findViewById(R.id.statusChip);
        statusLang = findViewById(R.id.statusLang);
        statusLine = findViewById(R.id.statusLine);
        fileTabLabel = findViewById(R.id.fileTabLabel);
        btnRun = findViewById(R.id.btnRun);
        consolePanel = findViewById(R.id.consolePanel);
        drawerLayout = findViewById(R.id.drawerLayout);
        explorerDrawer = findViewById(R.id.explorerDrawer);

        runner = new CodeRunner(this);
        currentLanguage = SettingsStore.get(this).defaultCodeLang();

        String openId = getIntent().getStringExtra(EXTRA_SESSION_ID);
        if (openId != null) {
            RecentSession s = RecentSessionStore.get(this).findById(openId);
            if (s != null) {
                currentSessionId = s.id;
                currentLanguage = s.language;
                currentFilename = s.filename;
                editor.setText(s.sourceCode);
            }
        }
        if (currentFilename == null) currentFilename = defaultFilename(currentLanguage);
        if (editor.getText().length() == 0) editor.setText(defaultCode(currentLanguage));
        fileTabLabel.setText(currentFilename);
        updateStatusBar();

        refreshLineNumbers();

        editor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                refreshLineNumbers();
                updateStatusBar();
                persistLocal();
            }
        });

        // Toolbar buttons
        TextView langBadge = findViewById(R.id.langBadge);
        langBadge.setText(currentLanguage.substring(0, 1).toUpperCase() + currentLanguage.substring(1));

        findViewById(R.id.btnUndo).setOnClickListener(v -> {
            // Simple undo - clear last char (placeholder)
        });
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            persistLocal();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnCopy).setOnClickListener(v -> {
            android.content.ClipboardManager clip = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clip.setPrimaryClip(android.content.ClipData.newPlainText("code", editor.getText().toString()));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, editor.getText().toString());
            startActivity(Intent.createChooser(share, "Share code"));
        });
        findViewById(R.id.btnFolder).setOnClickListener(v -> {
            drawerLayout.openDrawer(explorerDrawer);
        });

        // File tab new button
        findViewById(R.id.btnNew).setOnClickListener(v -> {
            currentSessionId = null;
            currentFilename = defaultFilename(currentLanguage);
            editor.setText(defaultCode(currentLanguage));
            fileTabLabel.setText(currentFilename);
            runner.resetSession();
            outputView.setText("");
            setStatus("Ready", Color.parseColor("#34D399"));
        });

        // File tab close
        findViewById(R.id.fileTabClose).setOnClickListener(v -> finish());

        // Console controls
        findViewById(R.id.btnToggleConsole).setOnClickListener(v -> toggleConsole());
        findViewById(R.id.btnCloseConsole).setOnClickListener(v -> hideConsole());
        findViewById(R.id.btnClearConsole).setOnClickListener(v -> outputView.setText(""));

        // Console tabs
        findViewById(R.id.consTabOutput).setOnClickListener(v -> {
            // Already showing output
        });
        findViewById(R.id.consTabProblems).setOnClickListener(v -> {
            // Could show problems/errors - placeholder
        });

        // Explorer new file
        findViewById(R.id.btnNewFileDrawer).setOnClickListener(v -> {
            drawerLayout.closeDrawer(explorerDrawer);
            currentSessionId = null;
            currentFilename = defaultFilename(currentLanguage);
            editor.setText(defaultCode(currentLanguage));
            fileTabLabel.setText(currentFilename);
            runner.resetSession();
        });

        btnRun.setOnClickListener(v -> onRunClicked());
        setStatus("Ready", Color.parseColor("#34D399"));

        // Console hidden initially
        consolePanel.setVisibility(View.GONE);

        ProfileStore.get(this).touchActivity();
    }

    private void toggleConsole() {
        if (consoleVisible) hideConsole(); else showConsole();
    }

    private void showConsole() {
        consolePanel.setVisibility(View.VISIBLE);
        consoleVisible = true;
    }

    private void hideConsole() {
        consolePanel.setVisibility(View.GONE);
        consoleVisible = false;
    }

    private void updateStatusBar() {
        if (statusLang != null) statusLang.setText(currentLanguage);
        if (statusLine != null) {
            int line = editor.getLayout() != null ? editor.getLayout().getLineForOffset(editor.getSelectionStart()) + 1 : 1;
            statusLine.setText("Ln " + line);
        }
    }

    private static String defaultCode(String lang) {
        if ("python".equalsIgnoreCase(lang)) return DEFAULT_CODE_PY;
        if ("javascript".equalsIgnoreCase(lang)) return DEFAULT_CODE_JS;
        return DEFAULT_CODE_JAVA;
    }

    private static String defaultFilename(String lang) {
        if ("python".equalsIgnoreCase(lang)) return "main.py";
        if ("javascript".equalsIgnoreCase(lang)) return "main.js";
        return "Main.java";
    }

    private void persistLocal() {
        currentSessionId = RecentSessionStore.get(this).save(
                currentSessionId,
                currentFilename,
                "Playground",
                currentLanguage,
                editor.getText().toString(),
                runner.backendSessionId());
    }

    private void refreshLineNumbers() {
        int lines = 1;
        String text = editor.getText().toString();
        for (int i = 0; i < text.length(); i++) if (text.charAt(i) == '\n') lines++;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) sb.append(i).append('\n');
        lineNumbers.setText(sb.toString());
    }

    private void onRunClicked() {
        btnRun.setEnabled(false);
        showConsole();
        outputView.setText("");
        runner.run(currentLanguage, editor.getText().toString(), new CodeRunner.Listener() {
            @Override public void onStatus(String text) {
                setStatus(text, Color.parseColor("#FBBF24"));
            }
            @Override public void onResult(ExecutionResponse r) {
                renderOutput(r);
                persistLocal();
                btnRun.setEnabled(true);
            }
            @Override public void onError(String message) {
                setStatus("Error", Color.parseColor("#F87171"));
                outputView.setText(message);
                btnRun.setEnabled(true);
                Toast.makeText(PlaygroundActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderOutput(ExecutionResponse er) {
        String s = er.status == null ? "" : er.status;
        int color;
        String label;
        switch (s) {
            case "COMPLETED":
                label = "Compiled OK";
                color = Color.parseColor("#34D399");
                if (!awardedFirstRun) {
                    ProfileStore.get(this).awardXp(10, false);
                    awardedFirstRun = true;
                }
                break;
            case "FAILED":  label = "Failed";    color = Color.parseColor("#F87171"); break;
            case "TIMEOUT": label = "Timed out"; color = Color.parseColor("#F87171"); break;
            default:        label = s;           color = Color.parseColor("#8DA1C4");
        }
        setStatus(label, color);

        StringBuilder sb = new StringBuilder();
        if (er.stdout != null && !er.stdout.isEmpty()) sb.append(er.stdout);
        if (er.stderr != null && !er.stderr.isEmpty()) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(er.stderr);
        }
        if (er.exitCode != null) {
            if (sb.length() > 0) sb.append('\n');
            sb.append("Process finished with exit code ").append(er.exitCode);
        }
        if (sb.length() == 0) sb.append("(no output)");
        outputView.setText(sb.toString());
    }

    private void setStatus(String text, int color) {
        statusChip.setText(text);
        statusChip.setTextColor(color);
    }
}
