package com.codelab.app.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.codelab.app.BuildConfig;
import com.codelab.app.R;
import com.codelab.app.api.dto.AuthResponse;
import com.codelab.app.data.AuthManager;
import com.codelab.app.ui.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.UUID;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private GoogleSignInClient googleClient;

    private final ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount acct = task.getResult(ApiException.class);
                    String idToken = acct != null ? acct.getIdToken() : null;
                    if (idToken == null) {
                        toast("Google returned no ID token");
                        return;
                    }
                    runBackend(() -> AuthManager.googleLoginSync(this, idToken));
                } catch (ApiException e) {
                    toast("Google sign-in failed (" + e.getStatusCode() + ")");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AuthManager.isLoggedIn(this)) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> doEmailLogin());
        findViewById(R.id.linkRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.linkGuest).setOnClickListener(v -> doGuestLogin());

        Button btnGoogle = findViewById(R.id.btnGoogle);
        Button btnGithub = findViewById(R.id.btnGithub);
        Button btnFacebook = findViewById(R.id.btnFacebook);

        if (TextUtils.isEmpty(BuildConfig.GOOGLE_WEB_CLIENT_ID)) {
            btnGoogle.setVisibility(View.GONE);
        } else {
            GoogleSignInOptions opts = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .requestEmail()
                    .build();
            googleClient = GoogleSignIn.getClient(this, opts);
            btnGoogle.setOnClickListener(v -> {
                googleClient.signOut().addOnCompleteListener(t ->
                        googleLauncher.launch(googleClient.getSignInIntent()));
            });
        }

        if (TextUtils.isEmpty(BuildConfig.GITHUB_CLIENT_ID)) {
            btnGithub.setVisibility(View.GONE);
        } else {
            btnGithub.setOnClickListener(v -> launchOAuth("github",
                    "https://github.com/login/oauth/authorize",
                    "?client_id=" + BuildConfig.GITHUB_CLIENT_ID +
                            "&redirect_uri=" + Uri.encode(redirectUri("github")) +
                            "&scope=read:user%20user:email" +
                            "&state="));
        }

        if (TextUtils.isEmpty(BuildConfig.FACEBOOK_APP_ID)) {
            btnFacebook.setVisibility(View.GONE);
        } else {
            btnFacebook.setOnClickListener(v -> launchOAuth("facebook",
                    "https://www.facebook.com/v18.0/dialog/oauth",
                    "?client_id=" + BuildConfig.FACEBOOK_APP_ID +
                            "&redirect_uri=" + Uri.encode(redirectUri("facebook")) +
                            "&response_type=token" +
                            "&scope=email,public_profile" +
                            "&state="));
        }

        // Demo accounts
        findViewById(R.id.demoAdmin).setOnClickListener(v -> fillDemo("admin@edtronaut.ai"));
        findViewById(R.id.demoCreator).setOnClickListener(v -> fillDemo("creator@edtronaut.ai"));
        findViewById(R.id.demoUser).setOnClickListener(v -> fillDemo("user@edtronaut.ai"));
    }

    private String redirectUri(String provider) {
        return BuildConfig.OAUTH_REDIRECT_SCHEME + "://oauth/" + provider;
    }

    private void launchOAuth(String provider, String baseUrl, String queryWithTrailingState) {
        String state = UUID.randomUUID().toString();
        String url = baseUrl + queryWithTrailingState + state;
        OAuthCallbackActivity.stashPending(this, provider, state, redirectUri(provider));
        try {
            CustomTabsIntent tab = new CustomTabsIntent.Builder().build();
            tab.launchUrl(this, Uri.parse(url));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    private void fillDemo(String email) {
        editEmail.setText(email);
        editPassword.setText("demo123");
    }

    private void doEmailLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            toast("Enter email and password");
            return;
        }
        runBackend(() -> AuthManager.loginSync(this, email, password));
    }

    private void doGuestLogin() {
        runBackend(() -> AuthManager.deviceLoginSync(this));
    }

    private interface AuthCall {
        Response<AuthResponse> call() throws Exception;
    }

    private void runBackend(AuthCall c) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Response<AuthResponse> resp = c.call();
                if (resp.isSuccessful() && resp.body() != null) {
                    AuthManager.persistAuth(this, resp.body());
                    runOnUiThread(this::goToMain);
                } else {
                    runOnUiThread(() -> toast("Login failed (" + resp.code() + ")"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> toast("Network error: " + e.getMessage()));
            }
        });
    }

    private void goToMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
