package com.codelab.app.ui.market;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.CatalogEntry;
import com.codelab.app.data.PackRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * "Manage Downloads" screen — lists only installed packs, lets the user
 * uninstall or set the active study pack. Distinct from MarketFragment which
 * is the browse-everything marketplace.
 */
public class MarketplaceActivity extends AppCompatActivity {

    private InstalledPackAdapter adapter;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        findViewById(R.id.marketBack).setOnClickListener(v -> finish());

        TextView title = findViewById(R.id.marketTitle);
        if (title != null) title.setText(R.string.title_manage_downloads);

        TextView subtitle = findViewById(R.id.marketSubtitle);
        if (subtitle != null) subtitle.setText(R.string.manage_downloads_subtitle);

        findViewById(R.id.browseMarketplaceBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, com.codelab.app.ui.MainActivity.class)
                    .putExtra(com.codelab.app.ui.MainActivity.EXTRA_START_TAB,
                              com.codelab.app.ui.MainActivity.TAB_MARKET));
        });

        emptyView = findViewById(R.id.emptyInstalled);
        RecyclerView rv = findViewById(R.id.marketRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InstalledPackAdapter(this);
        rv.setAdapter(adapter);
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<CatalogEntry> installed = new ArrayList<>(PackRepository.get(this).installedEntries());
        adapter.setItems(installed);
        emptyView.setVisibility(installed.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
