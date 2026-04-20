package com.codelab.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.codelab.app.R;
import com.codelab.app.data.AuthManager;
import com.codelab.app.data.ProfileStore;
import com.codelab.app.ui.auth.LoginActivity;
import com.codelab.app.ui.home.HomeFragment;
import com.codelab.app.ui.market.MarketFragment;
import com.codelab.app.ui.profile.ProfileFragment;
import com.codelab.app.ui.study.StudyFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_START_TAB = "start_tab";
    public static final int TAB_HOME = 0, TAB_STUDY = 1, TAB_CODE = 2, TAB_MARKET = 3, TAB_PROFILE = 4;

    private BottomNavigationView bottomNav;
    private int currentTabId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthManager.isLoggedIn(this)) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Mark today as an active day for streak tracking
        ProfileStore.get(this).touchActivity();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(this::onTabSelected);

        if (savedInstanceState == null) {
            int start = getIntent().getIntExtra(EXTRA_START_TAB, TAB_HOME);
            selectTab(start);
        }
    }

    private boolean onTabSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home)    { show(new HomeFragment());    currentTabId = id; return true; }
        if (id == R.id.nav_study)   { show(new StudyFragment());   currentTabId = id; return true; }
        if (id == R.id.nav_market)  { show(new MarketFragment());  currentTabId = id; return true; }
        if (id == R.id.nav_profile) { show(new ProfileFragment()); currentTabId = id; return true; }
        if (id == R.id.nav_code) {
            startActivity(new Intent(this, PlaygroundActivity.class));
            bottomNav.post(() -> {
                if (currentTabId != R.id.nav_code) {
                    bottomNav.getMenu().findItem(currentTabId).setChecked(true);
                }
            });
            return false;
        }
        return false;
    }

    public void selectTab(int tab) {
        switch (tab) {
            case TAB_HOME:    bottomNav.setSelectedItemId(R.id.nav_home);    break;
            case TAB_STUDY:   bottomNav.setSelectedItemId(R.id.nav_study);   break;
            case TAB_CODE:    bottomNav.setSelectedItemId(R.id.nav_code);    break;
            case TAB_MARKET:  bottomNav.setSelectedItemId(R.id.nav_market);  break;
            case TAB_PROFILE: bottomNav.setSelectedItemId(R.id.nav_profile); break;
        }
    }

    private void show(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .commit();
    }
}
