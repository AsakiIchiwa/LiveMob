package com.codelab.app.ui.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
 * Lists all saved coding sessions (projects). Tapping one opens it in Playground.
 */
public class ProjectsActivity extends AppCompatActivity {

    private RecentSessionAdapter adapter;
    private View emptyView;
    private RecyclerView recycler;

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
        adapter = new RecentSessionAdapter(sessions, s -> {
            Intent i = new Intent(this, PlaygroundActivity.class);
            i.putExtra(PlaygroundActivity.EXTRA_SESSION_ID, s.id);
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        boolean empty = sessions.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
