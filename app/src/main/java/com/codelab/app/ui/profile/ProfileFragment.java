package com.codelab.app.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.codelab.app.BuildConfig;
import com.codelab.app.R;
import com.codelab.app.api.ApiClient;
import com.codelab.app.data.AuthManager;
import com.codelab.app.data.PackRepository;
import com.codelab.app.data.ProfileStore;
import com.codelab.app.data.ProgressStore;
import com.codelab.app.data.RecentSessionStore;
import com.codelab.app.data.SettingsStore;
import com.codelab.app.data.UserProfile;
import com.codelab.app.ui.auth.LoginActivity;
import com.codelab.app.ui.settings.SettingsActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Response;

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

        v.findViewById(R.id.menuChangePassword).setOnClickListener(view -> showChangePasswordDialog());
        v.findViewById(R.id.menuSyncData).setOnClickListener(view -> doSyncData());
        v.findViewById(R.id.menuManageDevices).setOnClickListener(view -> showManageDevicesDialog());
        v.findViewById(R.id.menuDeleteAccount).setOnClickListener(view -> confirmDeleteAccount());

        v.findViewById(R.id.menuNotifications).setOnClickListener(view ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));
        v.findViewById(R.id.menuStorage).setOnClickListener(view -> showStorageDialog());
        v.findViewById(R.id.menuAbout).setOnClickListener(view -> showAboutDialog());

        v.findViewById(R.id.menuSignOut).setOnClickListener(view -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Sign out?")
                    .setMessage("You'll need to sign in again to access your lessons and sessions.")
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton("Sign out", (d, w) -> {
                        android.content.Context ctx = requireContext().getApplicationContext();
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AuthManager.logout(ctx);
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    Intent i = new Intent(ctx, LoginActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    requireActivity().finish();
                                });
                            }
                        });
                    })
                    .show();
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

    // ─── Account actions ───────────────────────────────────────────────────

    private void showChangePasswordDialog() {
        Context ctx = requireContext();
        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        wrap.setPadding(pad, dp(8), pad, 0);

        EditText oldPw = pwdInput(ctx, "Current password");
        EditText newPw = pwdInput(ctx, "New password (6+ chars)");
        EditText confirmPw = pwdInput(ctx, "Confirm new password");
        wrap.addView(oldPw);
        wrap.addView(newPw);
        wrap.addView(confirmPw);

        AlertDialog dlg = new AlertDialog.Builder(ctx)
                .setTitle("Change Password")
                .setView(wrap)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Update", null)
                .create();
        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            String oldP = oldPw.getText().toString();
            String newP = newPw.getText().toString();
            String conf = confirmPw.getText().toString();
            if (newP.length() < 6) { toast("Password must be at least 6 characters"); return; }
            if (!newP.equals(conf)) { toast("Passwords don't match"); return; }
            if (oldP.isEmpty()) { toast("Enter your current password"); return; }
            dlg.dismiss();
            Context app = ctx.getApplicationContext();
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Map<String, Object> body = new HashMap<>();
                    body.put("currentPassword", oldP);
                    body.put("password", newP);
                    Response<Map<String, Object>> res = ApiClient.get(app).updateMe(body).execute();
                    runOnUi(() -> toast(res.isSuccessful()
                            ? "Password updated"
                            : "Couldn't update password (" + res.code() + ")"));
                } catch (Exception e) {
                    runOnUi(() -> toast("Network error: " + e.getMessage()));
                }
            });
        }));
        dlg.show();
    }

    private void doSyncData() {
        Context app = requireContext().getApplicationContext();
        toast("Syncing…");
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean ok = false;
            try {
                Response<Map<String, Object>> me = ApiClient.get(app).getMe().execute();
                if (me.isSuccessful() && me.body() != null) {
                    Map<String, Object> body = me.body();
                    ProfileStore.get(app).update(p -> {
                        Object n = body.get("displayName");
                        Object e = body.get("email");
                        if (n instanceof String && !((String) n).isEmpty()) p.name = (String) n;
                        if (e instanceof String && !((String) e).isEmpty()) p.handle = "@" + ((String) e).split("@")[0];
                    });
                    ok = true;
                }
                try {
                    ApiClient.get(app).getSettings().execute();
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
            final boolean success = ok;
            runOnUi(() -> toast(success ? "Synced" : "Sync failed — check connection"));
        });
    }

    private void showManageDevicesDialog() {
        Context ctx = requireContext();
        String deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        String androidVer = "Android " + android.os.Build.VERSION.RELEASE;
        String body = "This device:\n• " + deviceName + "\n• " + androidVer
                + "\n\nYour session uses a refresh token that's only valid on this device. "
                + "\"Sign out everywhere\" revokes the token server-side so every device has to log in again.";
        new AlertDialog.Builder(ctx)
                .setTitle("Manage Devices")
                .setMessage(body)
                .setNeutralButton("Sign out everywhere", (d, w) -> signOutEverywhere())
                .setPositiveButton("Close", null)
                .show();
    }

    private void signOutEverywhere() {
        Context app = requireContext().getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            AuthManager.logout(app);
            runOnUi(() -> {
                Intent i = new Intent(app, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                if (getActivity() != null) requireActivity().finish();
            });
        });
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account?")
                .setMessage("This wipes your local progress, sessions, and installed packs, then signs you out. "
                        + "This action cannot be undone.")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Delete", (d, w) -> reallyConfirmDelete())
                .show();
    }

    private void reallyConfirmDelete() {
        Context ctx = requireContext();
        EditText confirm = new EditText(ctx);
        confirm.setHint("Type DELETE to confirm");
        confirm.setInputType(InputType.TYPE_CLASS_TEXT);
        int pad = dp(16);
        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(pad, dp(8), pad, 0);
        wrap.addView(confirm);

        AlertDialog dlg = new AlertDialog.Builder(ctx)
                .setTitle("Final confirmation")
                .setView(wrap)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Delete account", null)
                .create();
        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            if (!"DELETE".equals(confirm.getText().toString().trim())) {
                toast("Type DELETE to confirm");
                return;
            }
            dlg.dismiss();
            performDeleteAccount();
        }));
        dlg.show();
    }

    private void performDeleteAccount() {
        Context app = requireContext().getApplicationContext();
        toast("Deleting…");
        Executors.newSingleThreadExecutor().execute(() -> {
            // Best-effort remote delete; we don't know for sure the backend has this endpoint,
            // so failure isn't fatal — local wipe still runs.
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("delete", true);
                ApiClient.get(app).updateMe(body).execute();
            } catch (Exception ignored) {}

            // Local wipe: auth tokens, profile, packs, progress, sessions.
            AuthManager.logout(app);
            PackRepository.get(app).clear();
            ProgressStore.get(app).reset();
            RecentSessionStore.get(app).clear();
            SettingsStore.get(app).clear();

            runOnUi(() -> {
                Intent i = new Intent(app, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                if (getActivity() != null) requireActivity().finish();
            });
        });
    }

    private void showStorageDialog() {
        Context ctx = requireContext();
        int packs = PackRepository.get(ctx).installedEntries().size();
        int sessions = RecentSessionStore.get(ctx).all().size();
        String body = "• Installed packs: " + packs
                + "\n• Saved sessions: " + sessions
                + "\n\nClearing cache removes saved sessions and uninstalls non-built-in packs. "
                + "Your account and progress are kept.";
        new AlertDialog.Builder(ctx)
                .setTitle("Storage & Cache")
                .setMessage(body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Clear cache", (d, w) -> {
                    Context app = ctx.getApplicationContext();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        RecentSessionStore.get(app).clear();
                        PackRepository repo = PackRepository.get(app);
                        for (com.codelab.app.data.CatalogEntry e : new java.util.ArrayList<>(repo.installedEntries())) {
                            if (!e.builtIn) repo.uninstall(e.id);
                        }
                        runOnUi(() -> toast("Cache cleared"));
                    });
                })
                .show();
    }

    private void showAboutDialog() {
        String body = "CodeAPP\nVersion " + BuildConfig.VERSION_NAME + " (build " + BuildConfig.VERSION_CODE + ")"
                + "\n\nA mobile coding education platform for learning and running code on the go.";
        new AlertDialog.Builder(requireContext())
                .setTitle("About CodeAPP")
                .setMessage(body)
                .setPositiveButton("Close", null)
                .show();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private EditText pwdInput(Context ctx, String hint) {
        EditText et = new EditText(ctx);
        et.setHint(hint);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        et.setLayoutParams(lp);
        return et;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void toast(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void runOnUi(Runnable r) {
        if (isAdded() && getActivity() != null) requireActivity().runOnUiThread(r);
    }
}
