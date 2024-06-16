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
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.nio.ByteBuffer;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class OCR extends AppCompatActivity {
    
    private SurfaceView cameraPreview;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private ImageCapture imageCapture = null;
    private static final int PICK_IMAGE_REQUEST = 2;
    private ProcessCameraProvider cameraProvider; // Declare cameraProvider at the class level

    // Define an ActivityResultLauncher for selecting an image from the gallery at the class level
    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            // TODO: Use MLKit or other OCR library to process the selected image
            Log.d("OCR", "Image selected: " + uri);
        }
    });

    // Handle camera permissions request
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            initializeCamera();
        } else {
            // Handle permission denial
            Log.e("OCR", "Camera permission is required.");
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Log.d("OCR", "Image selected: " + imageUri);

            try {
                InputImage inputImage = InputImage.fromFilePath(getApplicationContext(), imageUri);
                processImageWithMLKit(inputImage, new TextRecognitionCallback() {
                    @Override
                    public void onTextRecognized(String recognizedText) {
                        // Handle the recognized text here
                        Log.d("OCR", "Extracted Text: " + recognizedText);

                        // Create an Intent to hold the result
                        Intent resultIntent = new Intent();
                        // Put the recognized text into the Intent
                        resultIntent.putExtra("recognizedText", recognizedText);
                        // Set the result of the activity
                        setResult(RESULT_OK, resultIntent);
                        // Finish the activity and return to the calling activity
                        finish();

                    }
    
                    @Override
                    public void onError(Exception e) {
                        // Handle errors here
                        Log.e("OCR", "Error recognizing text", e);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("OCR", "Error loading image from URI", e);
            }

            
        }
    }

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
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        // Capture image and process it
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {

            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                Log.d("OCR", "Image called back");
                InputImage inputImage = null;
                Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) {
                    // Create an InputImage instance from the Image object
                    inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                }

                if (inputImage == null) {
                    Log.e("OCR", "InputImage is null");
                    return;
                }

                processImageWithMLKit(inputImage, new TextRecognitionCallback() {
                    @Override
                    public void onTextRecognized(String recognizedText) {
                        // Handle the recognized text here
                        Log.d("OCR", "Extracted Text: " + recognizedText);

                        // Create an Intent to hold the result
                        Intent resultIntent = new Intent();
                        // Put the recognized text into the Intent
                        resultIntent.putExtra("recognizedText", recognizedText);
                        // Set the result of the activity
                        setResult(RESULT_OK, resultIntent);
                        // Finish the activity and return to the calling activity
                        finish();
                    }
                
                    @Override
                    public void onError(Exception e) {
                        // Handle errors here
                        Log.e("OCR", "Error recognizing text", e);
                    }
                });

                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }
                imageProxy.close(); // close the ImageProxy to free up resources

                // Return to the previous activity
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("OCR", "Image capture failed: " + exception.getMessage(), exception);
            }
            
        });
    }

    private @NonNull OutputFileOptions getOutputFileOptions(String timeStamp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_" + timeStamp + "_");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        return new OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        PreviewView cameraPreview = findViewById(R.id.camera_preview);
        Button captureButton = findViewById(R.id.capture_button);

        // Check for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            initializeCamera();
        }
        captureButton.setOnClickListener(v -> capturePhoto());

        Button selectImageButton = findViewById(R.id.select_image_button);
        selectImageButton.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

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

    private void processImageWithMLKit(InputImage image, TextRecognitionCallback callback) {
//        @SuppressLint("UnsafeOptInUsageError") Image.Plane[] planes = image.getPlanes();
//        ByteBuffer buffer = planes[0].getBuffer();
//        byte[] data = new byte[buffer.capacity()];
//        buffer.get(data);
//
//        InputImage inputImage = InputImage.fromByteBuffer(
//            ByteBuffer.wrap(data),
//            image.getWidth(),
//            image.getHeight(),
//            image.getImageInfo().getRotationDegrees(),
//            InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
//        );
    
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    
//        recognizer.process(inputImage)
        recognizer.process(image)
            .addOnSuccessListener(text -> {
                // Process recognized text
                String recognizedText = text.getText();
                Log.d("OCR", "Recognized Text: " + recognizedText);
                callback.onTextRecognized(recognizedText); // Use the callback to return the text
            })
            .addOnFailureListener(e -> {
                // Handle failure
                Log.e("OCR", "Text recognition failed", e);
                callback.onError(e); // Use the callback to return the error
            });
    }

}