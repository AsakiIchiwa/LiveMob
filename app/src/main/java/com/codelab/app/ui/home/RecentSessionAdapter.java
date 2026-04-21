package com.codelab.app.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.RecentSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RecentSessionAdapter extends RecyclerView.Adapter<RecentSessionAdapter.VH> {

    public interface OnClick { void onClick(RecentSession s); }
    public interface OnMenuClick { void onMenuClick(View anchor, RecentSession s); }

    private final List<RecentSession> items;
    private final OnClick listener;
    private OnMenuClick menuClickListener;

    /* ---- selection mode ---- */
    private boolean selectionMode = false;
    private final Set<String> selectedIds = new HashSet<>();

    public RecentSessionAdapter(List<RecentSession> items, OnClick listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setOnMenuClickListener(OnMenuClick l) { this.menuClickListener = l; }

    /* ---- selection helpers ---- */
    public void setSelectionMode(boolean on) {
        selectionMode = on;
        if (!on) selectedIds.clear();
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() { return selectionMode; }

    public void toggleSelection(String id) {
        if (selectedIds.contains(id)) selectedIds.remove(id);
        else selectedIds.add(id);
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (RecentSession s : items) selectedIds.add(s.id);
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public Set<String> getSelectedIds() { return selectedIds; }
    public int getSelectedCount() { return selectedIds.size(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_session, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RecentSession s = items.get(position);
        h.title.setText(s.filename);

        int fileCount = s.getFiles().size();
        String info = s.where + " · " + fileCount + (fileCount == 1 ? " file" : " files")
                + " · " + relative(s.timestampMs);
        h.subtitle.setText(info);

        /* checkbox visibility */
        h.checkbox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        h.checkbox.setChecked(selectedIds.contains(s.id));
        h.checkbox.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) selectedIds.add(s.id);
            else selectedIds.remove(s.id);
        });

        /* menu button visibility: hidden during selection mode */
        h.btnMenu.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
        h.btnMenu.setOnClickListener(v -> {
            if (menuClickListener != null) menuClickListener.onMenuClick(v, s);
        });

        /* item click */
        h.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(s.id);
            } else if (listener != null) {
                listener.onClick(s);
            }
        });

        /* long-press enters selection mode */
        h.itemView.setOnLongClickListener(v -> {
            if (!selectionMode && menuClickListener != null) {
                /* delegate to activity to enter selection mode */
                setSelectionMode(true);
                toggleSelection(s.id);
                return true;
            }
            return false;
        });

        /* selected highlight */
        h.itemView.setBackgroundResource(
                selectedIds.contains(s.id) ? R.drawable.bg_card_selected : R.drawable.bg_card);
    }

    @Override public int getItemCount() { return items.size(); }

    private static String relative(long ts) {
        long diff = System.currentTimeMillis() - ts;
        long mins = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (mins < 1) return "just now";
        if (mins < 60) return mins + "m ago";
        long hrs = TimeUnit.MILLISECONDS.toHours(diff);
        if (hrs < 24) return hrs + "h ago";
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days == 1) return "yesterday";
        if (days < 30) return days + "d ago";
        return "long ago";
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, subtitle;
        final CheckBox checkbox;
        final ImageView btnMenu;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.sessionTitle);
            subtitle = v.findViewById(R.id.sessionSubtitle);
            checkbox = v.findViewById(R.id.checkbox);
            btnMenu = v.findViewById(R.id.btnMenu);
        }
    }
}
