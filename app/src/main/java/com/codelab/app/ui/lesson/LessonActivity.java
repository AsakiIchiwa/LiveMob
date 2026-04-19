package com.codelab.app.ui.lesson;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codelab.app.R;
import com.codelab.app.api.dto.ExecutionResponse;
import com.codelab.app.data.Lesson;
import com.codelab.app.data.LessonPack;
import com.codelab.app.data.LessonRepository;
import com.codelab.app.data.PackRepository;
import com.codelab.app.data.ProfileStore;
import com.codelab.app.data.ProgressStore;
import com.codelab.app.data.RecentSessionStore;
import com.codelab.app.util.CodeRunner;

public class LessonActivity extends AppCompatActivity {

    public static final String EXTRA_PACK_ID = "pack_id";
    public static final String EXTRA_LESSON_INDEX = "lesson_index";

    private LessonPack pack;
    private Lesson lesson;
    private EditText editor;
    private TextView lineNumbers, outputView, statusChip, header, stepText, taskTextView, hintTextView;
    private View btnRun, btnSubmit;
    private CodeRunner runner;
    private String localSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        String packId = getIntent().getStringExtra(EXTRA_PACK_ID);
        if (packId == null) packId = PackRepository.get(this).activePackId();
        int index = getIntent().getIntExtra(EXTRA_LESSON_INDEX, 1);

        pack = packId == null ? null : PackRepository.get(this).loadPack(packId);
        lesson = (pack == null) ? null : LessonRepository.findByIndex(this, packId, index);
        if (lesson == null && pack != null && pack.lessons != null && !pack.lessons.isEmpty()) {
            lesson = pack.lessons.get(0);
        }

        editor = findViewById(R.id.lessonEditor);
        lineNumbers = findViewById(R.id.lessonLineNumbers);
        outputView = findViewById(R.id.lessonOutput);
        statusChip = findViewById(R.id.lessonStatusChip);
        header = findViewById(R.id.lessonHeader);
        stepText = findViewById(R.id.stepText);
        btnRun = findViewById(R.id.btnLessonRun);
        btnSubmit = findViewById(R.id.btnLessonSubmit);

        runner = new CodeRunner(this);

        if (pack == null || lesson == null) {
            Toast.makeText(this, "Lesson not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mark attempted as soon as it opens
        ProgressStore.get(this).markAttempted(pack.id, lesson.index);
        ProfileStore.get(this).touchActivity();

        header.setText("Lesson " + lesson.index + " · " + lesson.title);
        int total = (pack.lessons == null) ? 1 : pack.lessons.size();
        stepText.setText("Step " + lesson.index + " of " + total);

        // Find task + hint TextViews in the layout (they are static in XML, need ID-free lookup
        // since the layout uses generic TextViews — bind by walking).
        bindCalloutsByTraversal();

        editor.setText(lesson.starterCode);
        refreshLineNumbers();
        editor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                refreshLineNumbers();
                persistLocal();
            }
        });

        findViewById(R.id.btnLessonBack).setOnClickListener(v -> finish());
        btnRun.setOnClickListener(v -> onRun(false));
        btnSubmit.setOnClickListener(v -> onRun(true));

        setStatus("Ready", Color.parseColor("#34D399"));
    }

    private void bindCalloutsByTraversal() {
        View root = findViewById(android.R.id.content);
        TextView[] task = new TextView[1];
        TextView[] hint = new TextView[1];
        walk(root, view -> {
            if (!(view instanceof TextView)) return;
            TextView tv = (TextView) view;
            CharSequence cs = tv.getText();
            if (cs == null) return;
            String s = cs.toString();
            // The task body in the layout starts with "Declare an integer variable…"
            if (task[0] == null && s.startsWith("Declare an integer")) task[0] = tv;
            // The hint body starts with "Hint: Use int score"
            if (hint[0] == null && s.startsWith("Hint:")) hint[0] = tv;
        });
        if (task[0] != null) task[0].setText(lesson.task);
        if (hint[0] != null) hint[0].setText("Hint: " + (lesson.hint == null ? "" : lesson.hint));
        taskTextView = task[0];
        hintTextView = hint[0];
    }

    private interface ViewVisitor { void visit(View v); }

    private void walk(View v, ViewVisitor visitor) {
        visitor.visit(v);
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) walk(g.getChildAt(i), visitor);
        }
    }

    private void persistLocal() {
        localSessionId = RecentSessionStore.get(this).save(
                localSessionId,
                lesson.title,
                "Lesson " + lesson.index,
                pack.language,
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

    private void onRun(boolean isSubmit) {
        btnRun.setEnabled(false);
        btnSubmit.setEnabled(false);
        outputView.setText("");
        String code = editor.getText().toString();
        runner.run(pack.language, code, new CodeRunner.Listener() {
            @Override public void onStatus(String text) {
                setStatus(text, Color.parseColor("#FBBF24"));
            }
            @Override public void onResult(ExecutionResponse r) {
                renderOutput(r, isSubmit);
                persistLocal();
                btnRun.setEnabled(true);
                btnSubmit.setEnabled(true);
            }
            @Override public void onError(String message) {
                setStatus("Error", Color.parseColor("#F87171"));
                outputView.setText(message);
                btnRun.setEnabled(true);
                btnSubmit.setEnabled(true);
                Toast.makeText(LessonActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderOutput(ExecutionResponse er, boolean isSubmit) {
        String s = er.status == null ? "" : er.status;
        boolean ranOK = "COMPLETED".equals(s);
        boolean matches = ranOK && CodeRunner.outputMatches(lesson.expectedOutput, er.stdout);

        int color;
        String label;
        if (isSubmit && matches) {
            label = "✓ Expected output matches!";
            color = Color.parseColor("#34D399");
            // Mark completed + award XP only once
            ProgressStore progress = ProgressStore.get(this);
            boolean wasAlreadyCompleted = progress.isCompleted(pack.id, lesson.index);
            progress.markCompleted(pack.id, lesson.index);
            if (!wasAlreadyCompleted) {
                ProfileStore.get(this).awardXp(lesson.xpReward, true);
                Toast.makeText(this, "Lesson complete! +" + lesson.xpReward + " XP",
                        Toast.LENGTH_LONG).show();
            }
        } else if (isSubmit && ranOK) {
            label = "Output didn't match expected";
            color = Color.parseColor("#FBBF24");
        } else if (ranOK) {
            label = "Compiled OK";
            color = Color.parseColor("#34D399");
        } else if ("FAILED".equals(s))  { label = "Failed";    color = Color.parseColor("#F87171"); }
        else if ("TIMEOUT".equals(s))   { label = "Timed out"; color = Color.parseColor("#F87171"); }
        else                            { label = s;           color = Color.parseColor("#8DA1C4"); }

        setStatus(label, color);

        StringBuilder sb = new StringBuilder();
        if (er.stdout != null && !er.stdout.isEmpty()) sb.append(er.stdout);
        if (er.stderr != null && !er.stderr.isEmpty()) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(er.stderr);
        }
        if (isSubmit && ranOK && !matches && lesson.expectedOutput != null) {
            sb.append("\n\nExpected:\n").append(lesson.expectedOutput);
        }
        if (er.exitCode != null) {
            if (sb.length() > 0) sb.append('\n');
            sb.append("Exit code ").append(er.exitCode);
        }
        if (sb.length() == 0) sb.append("(no output)");
        outputView.setText(sb.toString());
    }

    private void setStatus(String text, int color) {
        statusChip.setText(text);
        statusChip.setTextColor(color);
    }
}
