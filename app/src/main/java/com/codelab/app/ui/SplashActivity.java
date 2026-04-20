package com.codelab.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.codelab.app.R;
import com.codelab.app.data.AuthManager;
import com.codelab.app.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent next;
            if (AuthManager.isLoggedIn(this)) {
                next = new Intent(this, MainActivity.class);
            } else {
                next = new Intent(this, LoginActivity.class);
            }
            startActivity(next);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
