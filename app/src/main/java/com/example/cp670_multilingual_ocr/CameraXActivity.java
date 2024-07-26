package com.example.cp670_multilingual_ocr;

import android.Manifest;

import android.content.ContentValues;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

        // Set up the capture button onClickListener
        Button captureButton = findViewById(R.id.capture_button);
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

                    Log.d(TAG, "Image object is not null");

                    // Create an InputImage instance from the Image object
                    int imageRotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                    Log.d(TAG, "Rotation degrees: " + imageRotationDegrees);

                    // Obtain input image from media storage
                    Log.d(TAG, "Obtain input image from media Stroage");
                    inputImage = InputImage.fromMediaImage(mediaImage, imageRotationDegrees);


                    // Convert InputImage to Bitmap
                    Log.d(TAG, "Convert InputImage to Bitmap");
                    Bitmap bitmap = inputImage.getBitmapInternal();

                    // Initialize Content Values
                    Log.d(TAG, "Initialize Content Values");
                    ContentValues values = new ContentValues();

                    // Put the image data into the Content Values
                    Log.d(TAG, "Put the image data into the Content Values");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, "captured_image_" + System.currentTimeMillis() + ".png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                    // Insert the image data into the Media Store and obtain imageUri
                    Log.d(TAG, "Insert the image data into the Media Store and obtain imageUri");
                    Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Log.d(TAG, "Image URI: " + imageUri);

                    // Save the bitmap to the gallery
                    Log.d(TAG, "Save the bitmap to the gallery");
                    try (OutputStream out = getContentResolver().openOutputStream(imageUri)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        Log.d(TAG, "Image saved to gallery");
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to save image to gallery", e);
                    }

                    // Return imageUri back to calling activity
                    Log.d(TAG, "Return imageUri back to calling activity");
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("imageUri", imageUri.toString());
                    setResult(RESULT_OK, resultIntent);
                    
                }

                // Check if the InputImage is null
                if (inputImage == null) { // InputImage is null
                    Log.e(TAG, "InputImage is null");
                    return;
                }

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