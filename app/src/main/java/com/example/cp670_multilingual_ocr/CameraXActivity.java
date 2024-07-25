package com.example.cp670_multilingual_ocr;

import android.Manifest;

import android.content.ContentValues;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.SurfaceView;
import android.widget.Button;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.OutputFileOptions;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.concurrent.ExecutionException;



public class CameraXActivity extends MainActivity {
    private static final String TAG = "CameraX";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private ImageCapture imageCapture = null;
    private SurfaceView cameraPreview;

    private ProcessCameraProvider cameraProvider; // Declare cameraProvider at the class level

    /*
     * Initialize an ActivityResultLauncher for requesting camera permissions
     * This launcher will be used to request camera permissions
     */
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            initializeCamera();
        } else {
            // Handle permission denial
            Log.e(TAG, "Camera permission is required.");
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "inside onCreate");

        super.onCreate(savedInstanceState);

        PreviewView cameraPreview = findViewById(R.id.camera_preview);
        Button captureButton = findViewById(R.id.capture_button);

        // Set up the capture button onClickListener
        captureButton.setOnClickListener(v -> capturePhoto());

        // Check for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { // Permission not granted

            // Request camera permissions
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);

        } else { // Permission granted

            // Initialize the camera
            initializeCamera();

        }

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_camerax;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check Camera permission
        if (requestCode == REQUEST_CAMERA_PERMISSION) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permission granted
                initializeCamera();
            }
            else { // Permission denied
                // Handle permission denial
                Log.e(TAG, "Camera permission was denied.");
            }

        }
    }

    /*
     * Initialize the camera
     */
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
                cameraProvider = cameraProviderFuture.get();
    
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
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));

    }

    /*
     * Capture a photo using the camera
     */
    private void capturePhoto() {
        if (imageCapture == null) return;

        // Capture image and process it
        //   - Use the ImageCapture.takePicture() method to capture an image
        //   - Use the ImageCapture.OnImageCapturedCallback() to handle the captured image
        //   - Use the processImageWithMLKit() method to process the captured image
        //   - Use the TextRecognitionCallback to handle the recognized text
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {

            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                Log.d(TAG, "Image called back");

                InputImage inputImage = null;

                // Get the Image object from the ImageProxy
                Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) { // Image object is not null
                    // Create an InputImage instance from the Image object
                    inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                }

                // Check if the InputImage is null
                if (inputImage == null) { // InputImage is null
                    Log.e("CameraX", "InputImage is null");
                    return;
                }

//                // Process the image using ML Kit
//                //   - Use the processImageWithMLKit() method to process the image
//                //   - Use the TextRecognitionCallback to handle the recognized text
//                processImageWithMLKit(inputImage, new TextRecognitionCallback() {
//                    @Override
//                    public void onTextRecognized(String recognizedText) {
//
//                        // Handle the recognized text here
//                        Log.d(TAG, "Extracted Text: " + recognizedText);
//
//                        // Create an Intent to hold the result
//                        Intent resultIntent = new Intent();
//
//                        // Put the recognized text into the Intent
//                        resultIntent.putExtra("recognizedText", recognizedText);
//
//                        // Set the result of the activity
//                        setResult(RESULT_OK, resultIntent);
//
//                        // Finish the activity and return to the calling activity
//                        finish();
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//
//                        // Handle errors here
//                        Log.e(TAG, "Error recognizing text", e);
//
//                    }
//                });

                // Unbind the cameraProvider and close the ImageProxy
                if (cameraProvider != null) { // cameraProvider is not null
                    cameraProvider.unbindAll();
                }

                // close the ImageProxy to free up resources
                imageProxy.close(); 

                // Return to the previous activity
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Image capture failed: " + exception.getMessage(), exception);
            }
            
        });
    }


}