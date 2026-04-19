package com.codelab.app.ui.study;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;
import com.codelab.app.data.Lesson;
import com.codelab.app.data.LessonPack;
import com.codelab.app.data.LessonRepository;
import com.codelab.app.data.PackRepository;
import com.codelab.app.data.ProgressStore;
import com.codelab.app.ui.lesson.LessonActivity;
import com.codelab.app.ui.market.MarketplaceActivity;

import java.util.ArrayList;
import java.util.List;

public class StudyFragment extends Fragment {

    private LessonAdapter adapter;
    private Lesson.Status filter = null; // null == all

    private TextView chipAll, chipInProgress, chipCompleted;
    private TextView activePackTitle, activePackProgress, activePackPercent;
    private ProgressBar activePackProgressBar;
    private RecyclerView recycler;
    private TextView emptyView;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        chipAll = v.findViewById(R.id.chipAll);
        chipInProgress = v.findViewById(R.id.chipInProgress);
        chipCompleted = v.findViewById(R.id.chipCompleted);
        activePackTitle = v.findViewById(R.id.activePackTitle);
        activePackProgress = v.findViewById(R.id.activePackProgress);
        activePackProgressBar = v.findViewById(R.id.activePackProgressBar);
        activePackPercent = v.findViewById(R.id.activePackPercent);
        recycler = v.findViewById(R.id.recyclerLessons);
        emptyView = v.findViewById(R.id.emptyLessons);

        chipAll.setOnClickListener(view -> setFilter(null));
        chipInProgress.setOnClickListener(view -> setFilter(Lesson.Status.IN_PROGRESS));
        chipCompleted.setOnClickListener(view -> setFilter(Lesson.Status.COMPLETED));

        v.findViewById(R.id.btnMarketplace).setOnClickListener(view ->
                startActivity(new Intent(requireContext(), MarketplaceActivity.class)));

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LessonAdapter(new ArrayList<>(), lesson -> {
            String packId = PackRepository.get(requireContext()).activePackId();
            Intent i = new Intent(requireContext(), LessonActivity.class);
            i.putExtra(LessonActivity.EXTRA_PACK_ID, packId);
            i.putExtra(LessonActivity.EXTRA_LESSON_INDEX, lesson.index);
            startActivity(i);
        });
        recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindActivePack();
        applyFilter();
    }

    private void bindActivePack() {
        LessonPack pack = LessonRepository.getActivePack(requireContext());
        if (pack == null) {
            activePackTitle.setText("No pack installed");
            activePackProgress.setText("Visit the marketplace to install lessons.");
            activePackProgressBar.setProgress(0);
            activePackPercent.setText("");
            return;
        }
        activePackTitle.setText(pack.title);

        int total = pack.lessons == null ? 0 : pack.lessons.size();
        int done = ProgressStore.get(requireContext()).countCompleted(pack.id, total);
        int pct = total == 0 ? 0 : (done * 100 / total);

        activePackProgress.setText("Current track · " + done + " of " + total + " lessons completed");
        activePackProgressBar.setProgress(pct);
        activePackPercent.setText(pct + "% complete");
    }

    private void setFilter(Lesson.Status status) {
        this.filter = status;
        chipAll.setSelected(status == null);
        chipInProgress.setSelected(status == Lesson.Status.IN_PROGRESS);
        chipCompleted.setSelected(status == Lesson.Status.COMPLETED);

        int onAccent = Color.parseColor("#041018");
        int secondary = Color.parseColor("#8DA1C4");
        chipAll.setTextColor(status == null ? onAccent : secondary);
        chipInProgress.setTextColor(status == Lesson.Status.IN_PROGRESS ? onAccent : secondary);
        chipCompleted.setTextColor(status == Lesson.Status.COMPLETED ? onAccent : secondary);

        applyFilter();
    }

    private void applyFilter() {
        List<Lesson> all = LessonRepository.getActive(requireContext());
        List<Lesson> shown = new ArrayList<>();
        for (Lesson l : all) {
            if (filter == null || l.status == filter) shown.add(l);
        }
        adapter.setItems(shown);

        boolean empty = shown.isEmpty();
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}
