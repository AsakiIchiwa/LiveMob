package com.codelab.app.ui.home;

import android.content.Intent;
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
import com.codelab.app.data.ProfileStore;
import com.codelab.app.data.ProgressStore;
import com.codelab.app.data.RecentSession;
import com.codelab.app.data.RecentSessionStore;
import com.codelab.app.ui.MainActivity;
import com.codelab.app.ui.PlaygroundActivity;
import com.codelab.app.ui.lesson.LessonActivity;
import com.codelab.app.ui.market.MarketplaceActivity;

import java.util.List;

public class HomeFragment extends Fragment {

    private TextView greetingText;
    private TextView continuePackTitle, continueLessonTitle, continueProgressText;
    private ProgressBar continueProgress;
    private View continueCard, btnResume;
    private RecyclerView recyclerSessions;
    private TextView emptyRecent;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        greetingText = v.findViewById(R.id.greetingText);
        continuePackTitle = v.findViewById(R.id.continuePackTitle);
        continueLessonTitle = v.findViewById(R.id.continueLessonTitle);
        continueProgress = v.findViewById(R.id.continueProgress);
        continueProgressText = v.findViewById(R.id.continueProgressText);
        continueCard = v.findViewById(R.id.continueCard);
        btnResume = v.findViewById(R.id.btnResume);
        recyclerSessions = v.findViewById(R.id.recyclerSessions);
        emptyRecent = v.findViewById(R.id.emptyRecent);

        v.findViewById(R.id.quickPlayground).setOnClickListener(view ->
                startActivity(new Intent(requireContext(), PlaygroundActivity.class)));
        v.findViewById(R.id.quickStudy).setOnClickListener(view ->
                ((MainActivity) requireActivity()).selectTab(MainActivity.TAB_STUDY));
        v.findViewById(R.id.quickMarket).setOnClickListener(view ->
                startActivity(new Intent(requireContext(), MarketplaceActivity.class)));

        v.findViewById(R.id.continueSeeAll).setOnClickListener(view ->
                ((MainActivity) requireActivity()).selectTab(MainActivity.TAB_STUDY));

        recyclerSessions.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        bindGreeting();
        bindContinueLearning();
        bindRecentSessions();
    }

    private void bindGreeting() {
        String name = ProfileStore.get(requireContext()).get().name;
        if (name == null || name.isEmpty()) name = "Coder";
        // Use first name only
        int sp = name.indexOf(' ');
        if (sp > 0) name = name.substring(0, sp);
        greetingText.setText(getString(R.string.greeting_format, name));
    }

    private void bindContinueLearning() {
        LessonPack pack = LessonRepository.getActivePack(requireContext());
        if (pack == null || pack.lessons == null || pack.lessons.isEmpty()) {
            continueCard.setVisibility(View.GONE);
            return;
        }
        continueCard.setVisibility(View.VISIBLE);
        continuePackTitle.setText(pack.title);

        ProgressStore progress = ProgressStore.get(requireContext());
        // Find next lesson: first not-completed, fallback to last
        Lesson next = pack.lessons.get(pack.lessons.size() - 1);
        for (Lesson l : pack.lessons) {
            if (!progress.isCompleted(pack.id, l.index)) { next = l; break; }
        }

        continueLessonTitle.setText("Lesson " + next.index + " · " + next.title);

        int total = pack.lessons.size();
        int done = progress.countCompleted(pack.id, total);
        int pct = total == 0 ? 0 : (done * 100 / total);
        continueProgress.setProgress(pct);
        continueProgressText.setText(pct + "% complete");

        final Lesson nextFinal = next;
        final String packId = pack.id;
        View.OnClickListener resume = view -> {
            Intent i = new Intent(requireContext(), LessonActivity.class);
            i.putExtra(LessonActivity.EXTRA_PACK_ID, packId);
            i.putExtra(LessonActivity.EXTRA_LESSON_INDEX, nextFinal.index);
            startActivity(i);
        };
        btnResume.setOnClickListener(resume);
        continueCard.setOnClickListener(resume);
    }

    private void bindRecentSessions() {
        List<RecentSession> sessions = RecentSessionStore.get(requireContext()).all();
        if (sessions.isEmpty()) {
            recyclerSessions.setVisibility(View.GONE);
            emptyRecent.setVisibility(View.VISIBLE);
            return;
        }
        recyclerSessions.setVisibility(View.VISIBLE);
        emptyRecent.setVisibility(View.GONE);
        recyclerSessions.setAdapter(new RecentSessionAdapter(sessions, s -> {
            Intent i = new Intent(requireContext(), PlaygroundActivity.class);
            i.putExtra(PlaygroundActivity.EXTRA_SESSION_ID, s.id);
            startActivity(i);
        }));
    }
}
