package com.codelab.app.ui.notifications;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.AppNotification;
import com.codelab.app.data.NotificationStore;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private NotificationAdapter adapter;
    private NotificationStore store;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        store = NotificationStore.get(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView markAll = findViewById(R.id.btnMarkAllRead);
        markAll.setOnClickListener(v -> {
            store.markAllRead();
            refresh();
        });

        emptyView = findViewById(R.id.emptyNotifications);
        RecyclerView rv = findViewById(R.id.notificationsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(store.all());
        rv.setAdapter(adapter);

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<AppNotification> list = store.all();
        adapter.setItems(list);

        boolean empty = list.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        findViewById(R.id.notificationsRecycler).setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
