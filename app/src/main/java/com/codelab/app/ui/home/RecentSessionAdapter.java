package com.codelab.app.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.RecentSession;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecentSessionAdapter extends RecyclerView.Adapter<RecentSessionAdapter.VH> {

    public interface OnClick { void onClick(RecentSession s); }

    private final List<RecentSession> items;
    private final OnClick listener;

    public RecentSessionAdapter(List<RecentSession> items, OnClick listener) {
        this.items = items;
        this.listener = listener;
    }

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
        h.subtitle.setText(s.where + " · " + relative(s.timestampMs));
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(s); });
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
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.sessionTitle);
            subtitle = v.findViewById(R.id.sessionSubtitle);
        }
    }
}
