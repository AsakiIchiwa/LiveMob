package com.codelab.app.ui.market;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.PackRepository;

public class MarketplaceActivity extends AppCompatActivity {

    private MarketPackAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        findViewById(R.id.marketBack).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.marketRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MarketPackAdapter(this, PackRepository.get(this).catalog(), entry -> {
            // toggle install / uninstall
            PackRepository repo = PackRepository.get(this);
            if (repo.isInstalled(entry.id)) {
                repo.uninstall(entry.id);
            } else {
                repo.install(entry.id);
                // If no active pack set or it's no longer installed, switch to this one
                if (repo.activePackId() == null || !repo.isInstalled(repo.activePackId())) {
                    repo.setActivePackId(entry.id);
                }
            }
            adapter.notifyDataSetChanged();
        });
        rv.setAdapter(adapter);
    }
}
