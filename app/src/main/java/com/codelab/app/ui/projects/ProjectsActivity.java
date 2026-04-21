package com.codelab.app.ui.projects;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.RecentSession;
import com.codelab.app.data.RecentSessionStore;
import com.codelab.app.ui.PlaygroundActivity;
import com.codelab.app.ui.home.RecentSessionAdapter;

import java.util.List;

/**
 * Lists all saved coding sessions (projects). Tap opens in Playground.
 * Long-press shows rename / delete options.
 */
public class ProjectsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnNewProject).setOnClickListener(v ->
                startActivity(new Intent(this, PlaygroundActivity.class)));

        emptyView = findViewById(R.id.emptyProjects);
        recycler = findViewById(R.id.projectsRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<RecentSession> sessions = RecentSessionStore.get(this).all();
        RecentSessionAdapter adapter = new RecentSessionAdapter(sessions, s -> {
            Intent i = new Intent(this, PlaygroundActivity.class);
            i.putExtra(PlaygroundActivity.EXTRA_SESSION_ID, s.id);
            startActivity(i);
        });
        adapter.setOnLongClickListener(this::showProjectMenu);
        recycler.setAdapter(adapter);

        boolean empty = sessions.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showProjectMenu(View anchor, RecentSession session) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, R.string.action_rename);
        popup.getMenu().add(0, 2, 1, R.string.action_delete);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showRenameDialog(session);
                return true;
            } else if (item.getItemId() == 2) {
                showDeleteDialog(session);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showRenameDialog(RecentSession session) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(session.filename);
        input.selectAll();

        new AlertDialog.Builder(this)
                .setTitle(R.string.action_rename)
                .setView(input)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        RecentSessionStore.get(this).rename(session.id, name);
                        refresh();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showDeleteDialog(RecentSession session) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_delete)
                .setMessage(getString(R.string.confirm_delete_project, session.filename))
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    RecentSessionStore.get(this).delete(session.id);
                    refresh();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
