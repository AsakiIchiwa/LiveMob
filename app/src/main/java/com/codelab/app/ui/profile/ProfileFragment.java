package com.codelab.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.codelab.app.R;
import com.codelab.app.data.ProfileStore;
import com.codelab.app.data.RecentSessionStore;
import com.codelab.app.data.UserProfile;
import com.codelab.app.ui.settings.SettingsActivity;

public class ProfileFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        v.findViewById(R.id.btnSettings).setOnClickListener(view ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        v.findViewById(R.id.menuEditProfile).setOnClickListener(view ->
                startActivity(new Intent(requireContext(), ProfileEditActivity.class)));

        v.findViewById(R.id.menuEditorSettings).setOnClickListener(view ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        v.findViewById(R.id.menuSignOut).setOnClickListener(view -> {
            // Clear auth and restart - placeholder
            android.widget.Toast.makeText(requireContext(), "Signed out", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = requireView();
        UserProfile p = ProfileStore.get(requireContext()).get();

        // Avatar initial
        TextView initial = v.findViewById(R.id.avatarInitial);
        if (p.name != null && !p.name.isEmpty()) {
            initial.setText(String.valueOf(p.name.charAt(0)).toUpperCase());
        } else {
            initial.setText("?");
        }

        ((TextView) v.findViewById(R.id.profileName)).setText(p.name != null ? p.name : "Coder");
        ((TextView) v.findViewById(R.id.profileEmail)).setText(p.handle != null ? p.handle : "device-user");
        ((TextView) v.findViewById(R.id.profileBadge)).setText("Free Tier");

        // Stats
        int projects = RecentSessionStore.get(requireContext()).all().size();
        ((TextView) v.findViewById(R.id.statProjects)).setText(String.valueOf(projects));
        ((TextView) v.findViewById(R.id.statRuns)).setText(String.valueOf(p.tasksCompleted));
        ((TextView) v.findViewById(R.id.statLessons)).setText(String.valueOf(p.xp / 10));
    }
}
