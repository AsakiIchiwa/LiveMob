package com.codelab.app.ui.study;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.Lesson;

import java.util.List;
import java.util.Locale;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.VH> {

    public interface OnClick { void onClick(Lesson lesson); }

    private List<Lesson> items;
    private final OnClick listener;

    public LessonAdapter(List<Lesson> items, OnClick listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<Lesson> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Lesson l = items.get(position);
        h.index.setText(String.format(Locale.US, "%02d", l.index));
        h.title.setText(l.title);
        h.subtitle.setText(l.subtitle);

        switch (l.status) {
            case COMPLETED:
                h.status.setText("Done");
                h.status.setTextColor(Color.parseColor("#34D399"));
                h.root.setBackgroundResource(R.drawable.bg_card_alt);
                break;
            case IN_PROGRESS:
                h.status.setText("In progress");
                h.status.setTextColor(Color.parseColor("#22D3EE"));
                h.root.setBackgroundResource(R.drawable.bg_card_selected);
                break;
            default:
                h.status.setText("");
                h.root.setBackgroundResource(R.drawable.bg_card_alt);
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(l); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final View root;
        final TextView index, title, subtitle, status;
        VH(@NonNull View v) {
            super(v);
            root = v.findViewById(R.id.lessonRoot);
            index = v.findViewById(R.id.lessonIndex);
            title = v.findViewById(R.id.lessonTitle);
            subtitle = v.findViewById(R.id.lessonSubtitle);
            status = v.findViewById(R.id.lessonStatus);
        }
    }
}
