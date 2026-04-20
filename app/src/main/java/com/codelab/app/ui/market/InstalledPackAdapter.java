package com.codelab.app.ui.market;

import android.app.AlertDialog;
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

public class InstalledPackAdapter extends RecyclerView.Adapter<InstalledPackAdapter.VH> {

    private final Context ctx;
    private List<CatalogEntry> items = new ArrayList<>();

    public InstalledPackAdapter(Context ctx) { this.ctx = ctx; }

    public void setItems(List<CatalogEntry> list) {
        this.items = list == null ? new ArrayList<>() : new ArrayList<>(list);
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
        CatalogEntry e = items.get(position);
        PackRepository repo = PackRepository.get(ctx);
        boolean isActive = e.id.equals(repo.activePackId());
        boolean isLanguage = "language".equals(e.type);

        h.title.setText(e.title);
        h.description.setText(e.description);
        h.lang.setText(e.language.toUpperCase(Locale.US));

        String meta = isLanguage
                ? "Runtime · Always available"
                : e.difficulty + " · " + e.lessonCount + " lessons · " + e.sizeKb + " KB"
                        + (isActive ? " · Active" : "");
        h.meta.setText(meta);

        if (isLanguage) {
            h.action.setText("Built-in");
            h.action.setBackgroundResource(R.drawable.bg_pill_outline);
            h.action.setTextColor(ctx.getResources().getColor(R.color.text_secondary));
            h.action.setClickable(false);
            h.itemView.setOnClickListener(null);
        } else if (e.builtIn) {
            h.action.setText(isActive ? "Active" : "Set active");
            h.action.setBackgroundResource(isActive
                    ? R.drawable.bg_pill_outline : R.drawable.bg_pill_accent);
            h.action.setTextColor(ctx.getResources().getColor(
                    isActive ? R.color.text_secondary : R.color.text_on_accent));
            h.action.setClickable(!isActive);
            if (!isActive) {
                h.action.setOnClickListener(v -> {
                    repo.setActivePackId(e.id);
                    notifyDataSetChanged();
                });
            }
            h.itemView.setOnClickListener(null);
        } else {
            h.action.setText(isActive ? "Active" : "Set active");
            h.action.setBackgroundResource(isActive
                    ? R.drawable.bg_pill_outline : R.drawable.bg_pill_accent);
            h.action.setTextColor(ctx.getResources().getColor(
                    isActive ? R.color.text_secondary : R.color.text_on_accent));
            h.action.setClickable(true);
            h.action.setOnClickListener(v -> {
                if (isActive) return;
                repo.setActivePackId(e.id);
                notifyDataSetChanged();
            });
            h.itemView.setOnLongClickListener(v -> {
                showUninstallDialog(e);
                return true;
            });
        }
    }

    private void showUninstallDialog(CatalogEntry e) {
        new AlertDialog.Builder(ctx)
                .setTitle("Uninstall " + e.title + "?")
                .setMessage("Frees up " + e.sizeKb + " KB. You can reinstall it from the Marketplace tab.")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_uninstall, (d, w) -> {
                    PackRepository.get(ctx).uninstall(e.id);
                    items.remove(e);
                    notifyDataSetChanged();
                })
                .show();
    }

    @Override public int getItemCount() { return items.size(); }

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
