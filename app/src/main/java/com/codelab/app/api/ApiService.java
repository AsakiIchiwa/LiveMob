package com.codelab.app.api;

import com.codelab.app.api.dto.AuthResponse;
import com.codelab.app.api.dto.CreateSessionRequest;
import com.codelab.app.api.dto.CreateSessionResponse;
import com.codelab.app.api.dto.DeviceLoginRequest;
import com.codelab.app.api.dto.ExecutionResponse;
import com.codelab.app.api.dto.RunRequest;
import com.codelab.app.api.dto.RunResponse;
import com.codelab.app.api.dto.UpdateSessionRequest;
import com.codelab.app.api.dto.UpdateSessionResponse;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ──── Auth ────
    @POST("api/v1/auth/device-login")
    Call<AuthResponse> deviceLogin(@Body DeviceLoginRequest body);

    @POST("api/v1/auth/refresh")
    Call<AuthResponse> refreshToken(@Body Map<String, String> body);

    @POST("api/v1/auth/logout")
    Call<ResponseBody> logout();

    @GET("api/v1/auth/me")
    Call<Map<String, Object>> getMe();

    @PATCH("api/v1/users/me")
    Call<Map<String, Object>> updateMe(@Body Map<String, Object> body);

    // ──── User Settings ────
    @GET("api/v1/users/me/settings")
    Call<Map<String, Object>> getSettings();

    @PATCH("api/v1/users/me/settings")
    Call<Map<String, Object>> updateSettings(@Body Map<String, Object> body);

    // ──── Code Sessions ────
    @POST("api/v1/code-sessions")
    Call<CreateSessionResponse> createSession(@Body CreateSessionRequest body);

    @PATCH("api/v1/code-sessions/{id}")
    Call<UpdateSessionResponse> updateSession(@Path("id") String id, @Body UpdateSessionRequest body);

    @POST("api/v1/code-sessions/{id}/run")
    Call<RunResponse> runSession(@Path("id") String id, @Body RunRequest body);

    @GET("api/v1/executions/{id}")
    Call<ExecutionResponse> getExecution(@Path("id") String id);

    // ──── Language Packs ────
    @GET("api/v1/language-packs")
    Call<List<Map<String, Object>>> getLanguagePacks(@Query("search") String search);

    @GET("api/v1/language-packs/{pack_id}")
    Call<Map<String, Object>> getLanguagePack(@Path("pack_id") String packId);

    @POST("api/v1/language-packs/{pack_id}/unlock")
    Call<Map<String, Object>> unlockLanguagePack(@Path("pack_id") String packId);

    @POST("api/v1/language-packs/{pack_id}/install")
    Call<Map<String, Object>> installLanguagePack(@Path("pack_id") String packId);

    @GET("api/v1/users/me/language-packs")
    Call<List<Map<String, Object>>> getUserLanguagePacks();

    @DELETE("api/v1/users/me/language-packs/{pack_id}")
    Call<ResponseBody> uninstallLanguagePack(@Path("pack_id") String packId);

    @GET("api/v1/language-packs/{pack_id}/manifest")
    Call<Map<String, Object>> getLanguagePackManifest(@Path("pack_id") String packId);

    // ──── Lesson Packs ────
    @GET("api/v1/lesson-packs")
    Call<List<Map<String, Object>>> getLessonPacks(@Query("search") String search);

    @GET("api/v1/lesson-packs/{pack_id}")
    Call<Map<String, Object>> getLessonPack(@Path("pack_id") String packId);

    @POST("api/v1/lesson-packs/{pack_id}/unlock")
    Call<Map<String, Object>> unlockLessonPack(@Path("pack_id") String packId);

    @GET("api/v1/users/me/lesson-packs")
    Call<List<Map<String, Object>>> getUserLessonPacks();

    @GET("api/v1/lesson-packs/{pack_id}/manifest")
    Call<Map<String, Object>> getLessonPackManifest(@Path("pack_id") String packId);

    @GET("api/v1/lesson-packs/{pack_id}/lessons")
    Call<List<Map<String, Object>>> getLessonPackLessons(@Path("pack_id") String packId);

    @GET("api/v1/lessons/{lesson_id}")
    Call<Map<String, Object>> getLesson(@Path("lesson_id") String lessonId);

    // ──── Progress ────
    @GET("api/v1/users/me/progress")
    Call<Map<String, Object>> getProgress();

    @GET("api/v1/users/me/progress/lesson-packs/{pack_id}")
    Call<Map<String, Object>> getPackProgress(@Path("pack_id") String packId);

    @GET("api/v1/users/me/progress/lessons/{lesson_id}")
    Call<Map<String, Object>> getLessonProgress(@Path("lesson_id") String lessonId);

    @PATCH("api/v1/users/me/progress/lessons/{lesson_id}")
    Call<Map<String, Object>> updateLessonProgress(@Path("lesson_id") String lessonId, @Body Map<String, Object> body);

    @POST("api/v1/lessons/{lesson_id}/complete")
    Call<Map<String, Object>> completeLesson(@Path("lesson_id") String lessonId);

    // ──── Submissions ────
    @POST("api/v1/lessons/{lesson_id}/submissions")
    Call<Map<String, Object>> createSubmission(@Path("lesson_id") String lessonId, @Body Map<String, Object> body);

    @GET("api/v1/submissions/{submission_id}")
    Call<Map<String, Object>> getSubmission(@Path("submission_id") String submissionId);

    @GET("api/v1/lessons/{lesson_id}/submissions")
    Call<List<Map<String, Object>>> getLessonSubmissions(@Path("lesson_id") String lessonId);

    @GET("api/v1/submissions/{submission_id}/result")
    Call<Map<String, Object>> getSubmissionResult(@Path("submission_id") String submissionId);

    // ──── Marketplace ────
    @GET("api/v1/marketplace")
    Call<List<Map<String, Object>>> getMarketplace(@Query("search") String search, @Query("type") String type);

    @GET("api/v1/marketplace/{submission_id}")
    Call<Map<String, Object>> getMarketplaceDetail(@Path("submission_id") String submissionId);

    // ──── System ────
    @GET("api/v1/system/supported-languages")
    Call<List<Map<String, Object>>> getSupportedLanguages();

    @GET("api/v1/system/status")
    Call<Map<String, Object>> getSystemStatus();
}
