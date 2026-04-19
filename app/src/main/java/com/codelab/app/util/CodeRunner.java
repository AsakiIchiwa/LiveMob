package com.codelab.app.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.codelab.app.api.ApiClient;
import com.codelab.app.api.ApiService;
import com.codelab.app.api.dto.CreateSessionRequest;
import com.codelab.app.api.dto.CreateSessionResponse;
import com.codelab.app.api.dto.ExecutionResponse;
import com.codelab.app.api.dto.RunRequest;
import com.codelab.app.api.dto.RunResponse;
import com.codelab.app.api.dto.UpdateSessionRequest;
import com.codelab.app.api.dto.UpdateSessionResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * End-to-end execution helper against the Live Code Execution backend.
 * Handles create → update → run → poll for one logical "session" tied to a CodeRunner instance.
 */
public class CodeRunner {

    public interface Listener {
        void onStatus(String text);
        void onResult(ExecutionResponse result);
        void onError(String message);
    }

    private final Context appCtx;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final String userId;
    private final String simulationId;

    private String backendSessionId;
    private int sessionVersion = 1;

    public CodeRunner(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
        this.userId = Prefs.getOrCreateUuid(appCtx, "user_id");
        this.simulationId = Prefs.getOrCreateUuid(appCtx, "simulation_id");
    }

    public void resetSession() {
        this.backendSessionId = null;
        this.sessionVersion = 1;
    }

    public String backendSessionId() { return backendSessionId; }

    public void run(String language, String code, Listener listener) {
        listener.onStatus("Starting…");
        if (backendSessionId == null) {
            createSessionThenRun(language, code, listener);
        } else {
            saveThenRun(code, listener);
        }
    }

    private ApiService api() { return ApiClient.get(appCtx); }

    private void createSessionThenRun(String language, String code, Listener listener) {
        CreateSessionRequest req = new CreateSessionRequest(simulationId, userId, language, code);
        api().createSession(req).enqueue(new Callback<CreateSessionResponse>() {
            @Override public void onResponse(Call<CreateSessionResponse> c, Response<CreateSessionResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    backendSessionId = r.body().sessionId;
                    sessionVersion = 1;
                    submitRun(listener);
                } else {
                    listener.onError("Could not start session (HTTP " + r.code() + ")");
                }
            }
            @Override public void onFailure(Call<CreateSessionResponse> c, Throwable t) {
                listener.onError("Network error");
            }
        });
    }

    private void saveThenRun(String code, Listener listener) {
        api().updateSession(backendSessionId, new UpdateSessionRequest(code, sessionVersion))
                .enqueue(new Callback<UpdateSessionResponse>() {
                    @Override public void onResponse(Call<UpdateSessionResponse> c, Response<UpdateSessionResponse> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            sessionVersion = r.body().version;
                            submitRun(listener);
                        } else if (r.code() == 409) {
                            sessionVersion++;
                            submitRun(listener);
                        } else {
                            listener.onError("Save failed (HTTP " + r.code() + ")");
                        }
                    }
                    @Override public void onFailure(Call<UpdateSessionResponse> c, Throwable t) {
                        listener.onError("Network error");
                    }
                });
    }

    private void submitRun(Listener listener) {
        listener.onStatus("Queued…");
        api().runSession(backendSessionId, new RunRequest(userId))
                .enqueue(new Callback<RunResponse>() {
                    @Override public void onResponse(Call<RunResponse> c, Response<RunResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().executionId != null) {
                            pollExecution(r.body().executionId, 0, listener);
                        } else if (r.code() == 429) {
                            listener.onError("Rate limited — try again shortly");
                        } else {
                            listener.onError("Run rejected (HTTP " + r.code() + ")");
                        }
                    }
                    @Override public void onFailure(Call<RunResponse> c, Throwable t) {
                        listener.onError("Network error");
                    }
                });
    }

    private void pollExecution(String executionId, int attempt, Listener listener) {
        if (attempt > 60) {
            listener.onError("Timed out waiting for result");
            return;
        }
        api().getExecution(executionId).enqueue(new Callback<ExecutionResponse>() {
            @Override public void onResponse(Call<ExecutionResponse> c, Response<ExecutionResponse> r) {
                if (!r.isSuccessful() || r.body() == null) {
                    listener.onError("Polling failed (HTTP " + r.code() + ")");
                    return;
                }
                ExecutionResponse er = r.body();
                String s = er.status == null ? "" : er.status;
                switch (s) {
                    case "QUEUED":
                        listener.onStatus("Queued…");
                        main.postDelayed(() -> pollExecution(executionId, attempt + 1, listener), 1000);
                        break;
                    case "RUNNING":
                        listener.onStatus("Running…");
                        main.postDelayed(() -> pollExecution(executionId, attempt + 1, listener), 1000);
                        break;
                    case "COMPLETED":
                    case "FAILED":
                    case "TIMEOUT":
                    case "CANCELLED":
                        listener.onResult(er);
                        break;
                    default:
                        main.postDelayed(() -> pollExecution(executionId, attempt + 1, listener), 1000);
                }
            }
            @Override public void onFailure(Call<ExecutionResponse> c, Throwable t) {
                listener.onError("Network error");
            }
        });
    }

    /** Compare actual stdout to expected — used by lessons to decide pass/fail. */
    public static boolean outputMatches(String expected, String actual) {
        if (expected == null) return false;
        String e = expected.replace("\r\n", "\n").trim();
        String a = (actual == null ? "" : actual).replace("\r\n", "\n").trim();
        return e.equals(a);
    }
}
