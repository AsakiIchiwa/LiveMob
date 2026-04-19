package com.codelab.app.ui.market;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.CatalogEntry;
import com.codelab.app.data.PackRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MarketPackAdapter extends RecyclerView.Adapter<MarketPackAdapter.VH> {

    public interface OnAction { void onClick(CatalogEntry entry); }

    private final Context ctx;
    private final List<CatalogEntry> allItems;
    private List<CatalogEntry> filteredItems;
    private final OnAction listener;
    private String currentFilter = "all";
    private String searchQuery = "";

    public MarketPackAdapter(Context ctx, List<CatalogEntry> items, OnAction listener) {
        this.ctx = ctx;
        this.allItems = items;
        this.filteredItems = new ArrayList<>(items);
        this.listener = listener;
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        applyFilters();
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query == null ? "" : query.toLowerCase(Locale.US);
        applyFilters();
    }

    private void applyFilters() {
        filteredItems = new ArrayList<>();
        for (CatalogEntry e : allItems) {
            // Filter by type
            if (!"all".equals(currentFilter)) {
                if ("language".equals(currentFilter) && !"language".equals(e.type)) continue;
                if ("lesson".equals(currentFilter) && !"lesson".equals(e.type)) continue;
            }
            // Filter by search
            if (!searchQuery.isEmpty()) {
                if (!e.title.toLowerCase(Locale.US).contains(searchQuery) &&
                    !e.language.toLowerCase(Locale.US).contains(searchQuery)) continue;
            }
            filteredItems.add(e);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_market_pack, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CatalogEntry e = filteredItems.get(position);
        h.title.setText(e.title);
        h.description.setText(e.description);
        h.lang.setText(e.language.toUpperCase(Locale.US));
        h.meta.setText(e.difficulty + " · " + e.lessonCount + " lessons · " + e.sizeKb + " KB");

        boolean installed = PackRepository.get(ctx).isInstalled(e.id);
        if (e.builtIn) {
            h.action.setText("Built-in");
            h.action.setBackgroundResource(R.drawable.bg_pill_outline);
            h.action.setTextColor(ctx.getResources().getColor(R.color.text_secondary));
            h.action.setOnClickListener(null);
            h.action.setClickable(false);
        } else if (installed) {
            h.action.setText(R.string.action_uninstall);
            h.action.setBackgroundResource(R.drawable.bg_pill_outline);
            h.action.setTextColor(ctx.getResources().getColor(R.color.text_secondary));
            h.action.setClickable(true);
            h.action.setOnClickListener(v -> { if (listener != null) listener.onClick(e); });
        } else {
            h.action.setText(R.string.action_install);
            h.action.setBackgroundResource(R.drawable.bg_pill_accent);
            h.action.setTextColor(ctx.getResources().getColor(R.color.text_on_accent));
            h.action.setClickable(true);
            h.action.setOnClickListener(v -> { if (listener != null) listener.onClick(e); });
        }
    }

    @Override public int getItemCount() { return filteredItems.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, description, lang, meta, action;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.packTitle);
            description = v.findViewById(R.id.packDescription);
            lang = v.findViewById(R.id.packLanguage);
            meta = v.findViewById(R.id.packMeta);
            action = v.findViewById(R.id.packAction);
        }
    }
}
