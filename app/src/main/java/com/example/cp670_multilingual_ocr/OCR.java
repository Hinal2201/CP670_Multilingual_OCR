package com.example.cp670_multilingual_ocr;

import android.Manifest;

import android.content.ContentValues;
import android.content.Context;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.Metadata;
import androidx.camera.core.ImageCapture.OutputFileOptions;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.Date;
import java.util.Locale;

public class OCR extends AppCompatActivity {
    
    private SurfaceView cameraPreview;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private ImageCapture imageCapture = null;

    private void initializeCamera() {

        final PreviewView previewView = findViewById(R.id.camera_preview);

        // Request camera permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
    
                // Preview
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Initialize ImageCapture
                imageCapture = new ImageCapture.Builder().build();

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();
    
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors (including cancellation)
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_" + timeStamp + "_");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        OutputFileOptions outputFileOptions = new OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {

            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Log.d("OCR", "Image saved: " + outputFileResults);
                // TODO: Call MLKit or other OCR library to process the saved image
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("OCR", "Image capture failed: " + exception.getMessage(), exception);
            }
        });
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        PreviewView cameraPreview = findViewById(R.id.camera_preview);
        Button captureButton = findViewById(R.id.capture_button);

        // Initialize your camera and start preview
        // Handle permission denial
        ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                initializeCamera();
            } else {
                // Handle permission denial
                Log.e("OCR", "Camera permission is required.");
            }
        });


        // Check for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            initializeCamera();
        }

        // captureButton.setOnClickListener(new View.OnClickListener() {
        //     public void onClick(View v) {
        //         // TODO: Capture photo and call MLKit to process the image
        //     }
        // });
        captureButton.setOnClickListener(v -> capturePhoto());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            }
            else {
                // Handle permission denial
                Log.e("OCR", "Camera permission was denied.");
            }
        }
    }

}