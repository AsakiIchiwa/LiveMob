package com.codelab.app.ui.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codelab.app.R;

public class CodeExplainActivity extends AppCompatActivity {

    private EditText inputEditor;
    private TextView outputView;
    private TextView counterView;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_explain);

        inputEditor = findViewById(R.id.explainInput);
        outputView = findViewById(R.id.explainOutput);
        counterView = findViewById(R.id.explainCounter);
        emptyState = findViewById(R.id.explainEmptyState);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnExplain).setOnClickListener(v -> explainCode());
        findViewById(R.id.btnClear).setOnClickListener(v -> clearAll());
        findViewById(R.id.btnPasteSample).setOnClickListener(v -> pasteSample());

        inputEditor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                counterView.setText(getString(R.string.code_explain_counter, s.length()));
            }
        });

        String initialCode = getIntent().getStringExtra(android.content.Intent.EXTRA_TEXT);
        if (initialCode != null && !initialCode.trim().isEmpty()) {
            inputEditor.setText(initialCode);
            inputEditor.setSelection(inputEditor.getText().length());
            counterView.setText(getString(R.string.code_explain_counter, initialCode.length()));
            explainCode();
        } else {
            counterView.setText(getString(R.string.code_explain_counter, 0));
            showEmptyState(true);
        }
    }

    private void explainCode() {
        String source = inputEditor.getText().toString().trim();
        if (source.isEmpty()) {
            Toast.makeText(this, R.string.code_explain_empty_input, Toast.LENGTH_SHORT).show();
            return;
        }
        String explanation = CodeInsightEngine.explain(source);
        outputView.setText(explanation);
        showEmptyState(false);
    }

    private void clearAll() {
        inputEditor.setText("");
        outputView.setText("");
        counterView.setText(getString(R.string.code_explain_counter, 0));
        showEmptyState(true);
    }

    private void pasteSample() {
        inputEditor.setText(
                "public int sum(int[] numbers) {\n" +
                "    int total = 0;\n" +
                "    for (int number : numbers) {\n" +
                "        total += number;\n" +
                "    }\n" +
                "    return total;\n" +
                "}\n"
        );
        inputEditor.setSelection(inputEditor.getText().length());
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        outputView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
