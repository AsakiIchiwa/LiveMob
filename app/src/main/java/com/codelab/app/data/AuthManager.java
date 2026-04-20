package com.codelab.app.data;

import android.content.Context;

import com.codelab.app.api.ApiClient;
import com.codelab.app.api.ApiService;
import com.codelab.app.api.dto.AuthResponse;
import com.codelab.app.api.dto.DeviceLoginRequest;
import com.codelab.app.api.dto.FacebookLoginRequest;
import com.codelab.app.api.dto.GithubLoginRequest;
import com.codelab.app.api.dto.GoogleLoginRequest;
import com.codelab.app.api.dto.LoginRequest;
import com.codelab.app.api.dto.RegisterRequest;
import com.codelab.app.util.Prefs;

import java.io.IOException;
import java.util.Map;

import retrofit2.Response;

/**
 * Thin façade over ApiService for auth flows. All methods run on the calling
 * thread — callers must invoke from a background thread (e.g. Executors).
 */
public final class AuthManager {
    public interface Callback {
        void onSuccess(AuthResponse auth);
        void onError(String message);
    }

    private AuthManager() {}

    /** @return true if a non-empty access token is stored. Does not validate expiry. */
    public static boolean isLoggedIn(Context ctx) {
        String token = SettingsStore.get(ctx).accessToken();
        return token != null && !token.isEmpty();
    }

    public static Response<AuthResponse> loginSync(Context ctx, String email, String password) throws IOException {
        ApiService api = ApiClient.get(ctx);
        return api.login(new LoginRequest(email, password)).execute();
    }

    public static Response<AuthResponse> registerSync(Context ctx, String email, String password, String displayName) throws IOException {
        ApiService api = ApiClient.get(ctx);
        return api.register(new RegisterRequest(email, password, displayName)).execute();
    }

    public static Response<AuthResponse> googleLoginSync(Context ctx, String idToken) throws IOException {
        ApiService api = ApiClient.get(ctx);
        return api.googleLogin(new GoogleLoginRequest(idToken)).execute();
    }

    public static Response<AuthResponse> githubLoginSync(Context ctx, String code, String redirectUri) throws IOException {
        ApiService api = ApiClient.get(ctx);
        return api.githubLogin(new GithubLoginRequest(code, redirectUri)).execute();
    }

    public static Response<AuthResponse> facebookLoginSync(Context ctx, String accessToken) throws IOException {
        ApiService api = ApiClient.get(ctx);
        return api.facebookLogin(new FacebookLoginRequest(accessToken)).execute();
    }

    public static Response<AuthResponse> deviceLoginSync(Context ctx) throws IOException {
        String deviceId = Prefs.getOrCreateUuid(ctx, "user_id");
        ApiService api = ApiClient.get(ctx);
        return api.deviceLogin(new DeviceLoginRequest(deviceId)).execute();
    }

    /** Persist tokens + mirror remote profile into local ProfileStore. Safe to call on background thread. */
    public static void persistAuth(Context ctx, AuthResponse auth) {
        SettingsStore store = SettingsStore.get(ctx);
        store.setAccessToken(auth.accessToken);
        if (auth.refreshToken != null) store.setRefreshToken(auth.refreshToken);

        if (auth.user != null) {
            String displayName = auth.user.displayName;
            ProfileStore.get(ctx).update(p -> {
                if (displayName != null && !displayName.isEmpty()) p.name = displayName;
                if (auth.user.email != null && !auth.user.email.isEmpty()) p.handle = "@" + auth.user.email.split("@")[0];
            });
        }

        // Best-effort: pull full profile (role, avatar, etc.) from /auth/me.
        try {
            Response<Map<String, Object>> me = ApiClient.get(ctx).getMe().execute();
            if (me.isSuccessful() && me.body() != null) {
                Map<String, Object> body = me.body();
                Object displayName = body.get("displayName");
                Object avatarUrl = body.get("avatarUrl");
                ProfileStore.get(ctx).update(p -> {
                    if (displayName instanceof String && !((String) displayName).isEmpty()) {
                        p.name = (String) displayName;
                    }
                    if (avatarUrl instanceof String) {
                        // avatarVariant left as-is; store URL if the model ever grows that field.
                    }
                });
            }
        } catch (Exception ignored) {}
    }

    /** Clears local tokens + profile + recent sessions so the next launch goes to Login. */
    public static void logout(Context ctx) {
        // Best-effort revoke remotely; don't block on failure.
        try {
            ApiClient.get(ctx).logout().execute();
        } catch (Exception ignored) {}

        SettingsStore store = SettingsStore.get(ctx);
        store.setAccessToken(null);
        store.setRefreshToken(null);
        ProfileStore.get(ctx).clear();
        ApiClient.invalidate();
    }
}
