package com.codelab.app.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.AppNotification;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private List<AppNotification> items;

    public NotificationAdapter(List<AppNotification> items) {
        this.items = items;
    }

    public void setItems(List<AppNotification> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AppNotification n = items.get(pos);
        h.title.setText(n.title);
        h.body.setText(n.body);
        h.time.setText(timeAgo(n.timestamp));
        h.dot.setVisibility(n.read ? View.INVISIBLE : View.VISIBLE);

        // Icon based on type
        int iconRes;
        switch (n.type) {
            case ACHIEVEMENT: iconRes = R.drawable.ic_trophy; break;
            case LESSON:      iconRes = R.drawable.ic_book; break;
            case SESSION:     iconRes = R.drawable.ic_code; break;
            default:          iconRes = R.drawable.ic_bell; break;
        }
        h.icon.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, body, time;
        View dot;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.notifIcon);
            title = v.findViewById(R.id.notifTitle);
            body = v.findViewById(R.id.notifBody);
            time = v.findViewById(R.id.notifTime);
            dot = v.findViewById(R.id.notifDot);
        }
    }

    private String timeAgo(long ts) {
        long diff = System.currentTimeMillis() - ts;
        if (diff < TimeUnit.MINUTES.toMillis(1)) return "Just now";
        if (diff < TimeUnit.HOURS.toMillis(1)) return (diff / TimeUnit.MINUTES.toMillis(1)) + "m ago";
        if (diff < TimeUnit.DAYS.toMillis(1)) return (diff / TimeUnit.HOURS.toMillis(1)) + "h ago";
        return (diff / TimeUnit.DAYS.toMillis(1)) + "d ago";
    }
}
