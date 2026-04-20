package com.codelab.app.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codelab.app.api.dto.AuthResponse;
import com.codelab.app.data.AuthManager;
import com.codelab.app.ui.MainActivity;

import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Deep-link target for OAuth redirects from GitHub and Facebook.
 *
 * GitHub uses authorization-code flow: redirect is `codelab://oauth/github?code=...&state=...`.
 * Facebook uses implicit (token) flow: redirect is `codelab://oauth/facebook#access_token=...&state=...`.
 */
public class OAuthCallbackActivity extends AppCompatActivity {
    static final String PREFS = "oauth_pending";
    static final String KEY_STATE = "state";
    static final String KEY_PROVIDER = "provider";
    static final String KEY_REDIRECT_URI = "redirect_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri data = getIntent() != null ? getIntent().getData() : null;
        if (data == null) {
            finish();
            return;
        }

        String path = data.getHost() != null ? data.getHost() : "";
        String providerFromPath = data.getLastPathSegment();
        // `codelab://oauth/github` → host=oauth, lastPathSegment=github
        String provider = "oauth".equals(path) ? providerFromPath : path;

        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String expectedState = sp.getString(KEY_STATE, null);
        String expectedProvider = sp.getString(KEY_PROVIDER, null);
        String redirectUri = sp.getString(KEY_REDIRECT_URI, null);
        sp.edit().clear().apply();

        if (!provider.equals(expectedProvider)) {
            toastAndFinish("OAuth provider mismatch");
            return;
        }

        String errorMsg = data.getQueryParameter("error_description");
        if (errorMsg == null) errorMsg = data.getQueryParameter("error");
        if (errorMsg != null) {
            toastAndFinish("Login cancelled: " + errorMsg);
            return;
        }

        if ("github".equals(provider)) {
            String code = data.getQueryParameter("code");
            String state = data.getQueryParameter("state");
            if (code == null || (expectedState != null && !expectedState.equals(state))) {
                toastAndFinish("Invalid GitHub OAuth response");
                return;
            }
            exchangeGithub(code, redirectUri);
        } else if ("facebook".equals(provider)) {
            String fragment = data.getFragment();
            String accessToken = null;
            String state = null;
            if (fragment != null) {
                for (String pair : fragment.split("&")) {
                    int eq = pair.indexOf('=');
                    if (eq <= 0) continue;
                    String k = pair.substring(0, eq);
                    String v = Uri.decode(pair.substring(eq + 1));
                    if ("access_token".equals(k)) accessToken = v;
                    else if ("state".equals(k)) state = v;
                }
            }
            if (accessToken == null || (expectedState != null && !expectedState.equals(state))) {
                toastAndFinish("Invalid Facebook OAuth response");
                return;
            }
            exchangeFacebook(accessToken);
        } else {
            toastAndFinish("Unknown OAuth provider");
        }
    }

    private void exchangeGithub(String code, String redirectUri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Response<AuthResponse> resp = AuthManager.githubLoginSync(this, code, redirectUri);
                onAuthResult(resp);
            } catch (Exception e) {
                runOnUiThread(() -> toastAndFinish("Network error: " + e.getMessage()));
            }
        });
    }

    private void exchangeFacebook(String accessToken) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Response<AuthResponse> resp = AuthManager.facebookLoginSync(this, accessToken);
                onAuthResult(resp);
            } catch (Exception e) {
                runOnUiThread(() -> toastAndFinish("Network error: " + e.getMessage()));
            }
        });
    }

    private void onAuthResult(Response<AuthResponse> resp) {
        if (resp.isSuccessful() && resp.body() != null) {
            AuthManager.persistAuth(this, resp.body());
            runOnUiThread(() -> {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            });
        } else {
            runOnUiThread(() -> toastAndFinish("Login failed (" + resp.code() + ")"));
        }
    }

    private void toastAndFinish(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    /** Stash state + provider + redirect URI before launching the OAuth browser tab. */
    public static void stashPending(android.content.Context ctx, String provider, String state, String redirectUri) {
        ctx.getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY_PROVIDER, provider)
                .putString(KEY_STATE, state)
                .putString(KEY_REDIRECT_URI, redirectUri)
                .apply();
    }

}
