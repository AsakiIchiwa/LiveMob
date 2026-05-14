package com.codelab.app.ui.tools;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.codelab.app.R;
import com.codelab.app.ui.PlaygroundActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class CodeScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView resultView;
    private TextView helperView;
    private View loadingView;
    private String latestAcceptedCode = "";
    private ExecutorService cameraExecutor;
    private final AtomicBoolean processingFrame = new AtomicBoolean(false);

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    helperView.setText(R.string.code_scanner_permission_denied);
                    Toast.makeText(this, R.string.code_scanner_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    processImageFromGallery(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_scanner);

        previewView = findViewById(R.id.cameraPreview);
        resultView = findViewById(R.id.scannerResult);
        helperView = findViewById(R.id.scannerHelper);
        loadingView = findViewById(R.id.scannerLoading);
        cameraExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnUploadImage).setOnClickListener(v -> openImagePicker());
        findViewById(R.id.btnRescan).setOnClickListener(v -> {
            latestAcceptedCode = "";
            resultView.setText("");
            helperView.setText(R.string.code_scanner_hint);
        });
        findViewById(R.id.btnUseCode).setOnClickListener(v -> openPlaygroundWithCode());

        ensureCameraPermission();
    }

    private void ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
                helperView.setText(R.string.code_scanner_hint);
            } catch (Exception e) {
                helperView.setText(R.string.code_scanner_camera_error);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        if (processingFrame.getAndSet(true)) {
            imageProxy.close();
            return;
        }
        if (imageProxy.getImage() == null) {
            processingFrame.set(false);
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(this::renderRecognizedText)
                .addOnFailureListener(error -> helperView.setText(R.string.code_scanner_scan_failed))
                .addOnCompleteListener(task -> {
                    processingFrame.set(false);
                    imageProxy.close();
                });
    }

    private void renderRecognizedText(Text text) {
        loadingView.setVisibility(View.GONE);
        String normalized = CodeInsightEngine.normalizeScannedCode(text.getText());
        if (normalized.isEmpty()) {
            helperView.setText(R.string.code_scanner_no_code_found);
            return;
        }

        latestAcceptedCode = normalized;
        resultView.setText(normalized);
        helperView.setText(R.string.code_scanner_review_prompt);
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void processImageFromGallery(Uri imageUri) {
        try {
            loadingView.setVisibility(View.VISIBLE);
            helperView.setText(R.string.code_scanner_processing);
            
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .addOnSuccessListener(this::renderRecognizedText)
                    .addOnFailureListener(error -> {
                        loadingView.setVisibility(View.GONE);
                        helperView.setText(R.string.code_scanner_scan_failed);
                        Toast.makeText(this, R.string.code_scanner_scan_failed, Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            loadingView.setVisibility(View.GONE);
            helperView.setText(R.string.code_scanner_scan_failed);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openPlaygroundWithCode() {
        if (latestAcceptedCode.trim().isEmpty()) {
            Toast.makeText(this, R.string.code_scanner_no_code_found, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, PlaygroundActivity.class);
        intent.putExtra(PlaygroundActivity.EXTRA_IMPORTED_CODE, latestAcceptedCode);
        intent.putExtra(PlaygroundActivity.EXTRA_IMPORTED_SOURCE, getString(R.string.title_code_scanner));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
