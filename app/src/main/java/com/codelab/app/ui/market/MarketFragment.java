package com.codelab.app.ui.market;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codelab.app.R;

public class MarketFragment extends Fragment {

    private TextView tabAll, tabLanguage, tabLesson;
    private EditText searchInput;
    private RecyclerView recycler;
    private String currentTab = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_market, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabAll = view.findViewById(R.id.tabAll);
        tabLanguage = view.findViewById(R.id.tabLanguage);
        tabLesson = view.findViewById(R.id.tabLesson);
        searchInput = view.findViewById(R.id.searchInput);
        recycler = view.findViewById(R.id.marketRecycler);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(new MarketPackAdapter(requireContext(), new java.util.ArrayList<>(), null));

        tabAll.setOnClickListener(v -> switchTab("all"));
        tabLanguage.setOnClickListener(v -> switchTab("language"));
        tabLesson.setOnClickListener(v -> switchTab("lesson"));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                filterList(s.toString());
            }
        });
    }

    private void switchTab(String tab) {
        currentTab = tab;
        int accent = getResources().getColor(R.color.accent, null);
        int secondary = getResources().getColor(R.color.text_secondary, null);

        tabAll.setTextColor(tab.equals("all") ? accent : secondary);
        tabLanguage.setTextColor(tab.equals("language") ? accent : secondary);
        tabLesson.setTextColor(tab.equals("lesson") ? accent : secondary);

        // Update adapter filter
        if (recycler.getAdapter() instanceof MarketPackAdapter) {
            ((MarketPackAdapter) recycler.getAdapter()).setFilter(tab);
        }
    }

    private void filterList(String query) {
        if (recycler.getAdapter() instanceof MarketPackAdapter) {
            ((MarketPackAdapter) recycler.getAdapter()).setSearchQuery(query);
        }
    }
}
