package com.codelab.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.RecentSession;

import java.util.List;

/**
 * Shows files inside the Playground explorer drawer.
 */
public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.VH> {

    public interface Listener {
        void onFileClick(int index);
        void onFileDelete(int index);
    }

    private List<RecentSession.ProjectFile> files;
    private int activeIndex;
    private final Listener listener;

    public ExplorerAdapter(List<RecentSession.ProjectFile> files, int activeIndex, Listener listener) {
        this.files = files;
        this.activeIndex = activeIndex;
        this.listener = listener;
    }

    public void update(List<RecentSession.ProjectFile> files, int activeIndex) {
        this.files = files;
        this.activeIndex = activeIndex;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_explorer_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        RecentSession.ProjectFile f = files.get(pos);
        h.name.setText(f.name);
        h.name.setTextColor(pos == activeIndex ? 0xFF4EC9B0 : 0xFFCCCCCC);
        h.icon.setImageResource(R.drawable.ic_file);
        h.itemView.setOnClickListener(v -> listener.onFileClick(pos));
        // Only allow delete if more than 1 file
        h.btnDelete.setVisibility(files.size() > 1 ? View.VISIBLE : View.GONE);
        h.btnDelete.setOnClickListener(v -> listener.onFileDelete(pos));
    }

    @Override public int getItemCount() { return files.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon, btnDelete;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.explorerFileName);
            icon = v.findViewById(R.id.explorerFileIcon);
            btnDelete = v.findViewById(R.id.explorerFileDelete);
        }
    }
}
