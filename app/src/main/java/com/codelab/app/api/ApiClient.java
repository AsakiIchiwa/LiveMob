package com.codelab.app.api;

import android.content.Context;

import com.codelab.app.BuildConfig;
import com.codelab.app.api.dto.AuthResponse;
import com.codelab.app.api.dto.DeviceLoginRequest;
import com.codelab.app.data.SettingsStore;
import com.codelab.app.util.Prefs;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
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
                                String token = SettingsStore.get(ctx).accessToken();
                                Request original = chain.request();

                                // Auto device-login if no token and not already an auth request
                                if ((token == null || token.isEmpty())
                                        && !original.url().encodedPath().contains("/auth/")) {
                                    try {
                                        String deviceId = Prefs.getOrCreateUuid(ctx, "user_id");
                                        OkHttpClient tempClient = new OkHttpClient.Builder()
                                                .connectTimeout(30, TimeUnit.SECONDS)
                                                .build();
                                        Retrofit tempRetrofit = new Retrofit.Builder()
                                                .baseUrl(SettingsStore.get(ctx).backendUrl())
                                                .client(tempClient)
                                                .addConverterFactory(GsonConverterFactory.create())
                                                .build();
                                        ApiService tempApi = tempRetrofit.create(ApiService.class);
                                        retrofit2.Response<AuthResponse> auth =
                                                tempApi.deviceLogin(new DeviceLoginRequest(deviceId)).execute();
                                        if (auth.isSuccessful() && auth.body() != null) {
                                            token = auth.body().accessToken;
                                            SettingsStore.get(ctx).setAccessToken(token);
                                        }
                                    } catch (Exception ignored) {}
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
                                    // Token expired — re-login
                                    String deviceId = Prefs.getOrCreateUuid(ctx, "user_id");
                                    try {
                                        OkHttpClient tempClient = new OkHttpClient.Builder()
                                                .connectTimeout(30, TimeUnit.SECONDS)
                                                .build();
                                        Retrofit tempRetrofit = new Retrofit.Builder()
                                                .baseUrl(SettingsStore.get(ctx).backendUrl())
                                                .client(tempClient)
                                                .addConverterFactory(GsonConverterFactory.create())
                                                .build();
                                        ApiService tempApi = tempRetrofit.create(ApiService.class);
                                        retrofit2.Response<AuthResponse> auth =
                                                tempApi.deviceLogin(new DeviceLoginRequest(deviceId)).execute();
                                        if (auth.isSuccessful() && auth.body() != null) {
                                            String newToken = auth.body().accessToken;
                                            SettingsStore.get(ctx).setAccessToken(newToken);
                                            return response.request().newBuilder()
                                                    .header("Authorization", "Bearer " + newToken)
                                                    .build();
                                        }
                                    } catch (Exception ignored) {}
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
