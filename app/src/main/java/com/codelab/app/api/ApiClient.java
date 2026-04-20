package com.codelab.app.api;

import android.content.Context;
import android.util.Base64;

import com.codelab.app.BuildConfig;
import com.codelab.app.api.dto.AuthResponse;
import com.codelab.app.data.SettingsStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONObject;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit client. Handles automatic anonymous device login
 * and injects Authorization Bearer tokens into all requests.
 */
public final class ApiClient {
    private static volatile ApiService INSTANCE;
    private static volatile String currentBaseUrl;

    private ApiClient() {}

    private static boolean isJwtExpired(String token) {
        if (token == null || token.isEmpty()) return true;

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;

            byte[] payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            JSONObject payload = new JSONObject(new String(payloadBytes, StandardCharsets.UTF_8));
            if (!payload.has("exp")) return false;

            long expiresAtSeconds = payload.getLong("exp");
            return System.currentTimeMillis() >= expiresAtSeconds * 1000L;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static AuthResponse loginWithRefreshToken(Context ctx, String refreshToken) {
        try {
            OkHttpClient tempClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            Retrofit tempRetrofit = new Retrofit.Builder()
                    .baseUrl(SettingsStore.get(ctx).backendUrl())
                    .client(tempClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService tempApi = tempRetrofit.create(ApiService.class);
            Map<String, String> body = new HashMap<>();
            body.put("refresh_token", refreshToken);
            retrofit2.Response<AuthResponse> auth = tempApi.refreshToken(body).execute();
            if (auth.isSuccessful() && auth.body() != null) {
                return auth.body();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String ensureAccessToken(Context ctx) {
        SettingsStore store = SettingsStore.get(ctx);
        String token = store.accessToken();

        if (token != null && !token.isEmpty() && !isJwtExpired(token)) {
            return token;
        }

        // Only try refresh. Do NOT silently fall back to device-login — that would
        // undo an explicit logout. Callers see a 401 and route to LoginActivity.
        String refreshToken = store.refreshToken();
        if (refreshToken != null && !refreshToken.isEmpty()) {
            AuthResponse auth = loginWithRefreshToken(ctx, refreshToken);
            if (auth != null && auth.accessToken != null && !auth.accessToken.isEmpty()) {
                store.setAccessToken(auth.accessToken);
                if (auth.refreshToken != null && !auth.refreshToken.isEmpty()) {
                    store.setRefreshToken(auth.refreshToken);
                }
                return auth.accessToken;
            }
        }

        return null;
    }

    public static ApiService get(Context ctx) {
        String url = SettingsStore.get(ctx).backendUrl();
        if (INSTANCE == null || !url.equals(currentBaseUrl)) {
            synchronized (ApiClient.class) {
                if (INSTANCE == null || !url.equals(currentBaseUrl)) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(45, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(chain -> {
                                Request original = chain.request();

                                String token = SettingsStore.get(ctx).accessToken();
                                if (!original.url().encodedPath().contains("/auth/")) {
                                    token = ensureAccessToken(ctx);
                                }

                                if (token == null || token.isEmpty()) return chain.proceed(original);

                                Request request = original.newBuilder()
                                        .header("Authorization", "Bearer " + token)
                                        .build();
                                return chain.proceed(request);
                            })
                            .authenticator(new Authenticator() {
                                @Override
                                public Request authenticate(Route route, Response response) throws IOException {
                                    String newToken = ensureAccessToken(ctx);
                                    if (newToken != null && !newToken.isEmpty()) {
                                    return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + newToken)
                                        .build();
                                    }
                                    return null;
                                }
                            })
                            .addInterceptor(log)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    INSTANCE = retrofit.create(ApiService.class);
                    currentBaseUrl = url;
                }
            }
        }
        return INSTANCE;
    }

    /** Force rebuild on next get() — call after Settings changes the URL. */
    public static void invalidate() {
        synchronized (ApiClient.class) {
            INSTANCE = null;
            currentBaseUrl = null;
        }
    }
}
