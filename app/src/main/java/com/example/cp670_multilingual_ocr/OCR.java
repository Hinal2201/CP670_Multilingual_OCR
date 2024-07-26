package com.example.cp670_multilingual_ocr;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.ImageCapture;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


public class OCR extends MainActivity {
    private static final String TAG = "OCRActivity";
    private static final int PICK_IMAGE_REQUEST = 2;

    /*  
     * Define an ActivityResultLauncher for selecting an image from the gallery at the class level
     * This launcher will be used to select an image from the gallery
     */
    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            // TODO: Use MLKit or other OCR library to process the selected image
            Log.d(TAG, "Image selected: " + uri);
        
            processImageUri(uri);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "inside onCreate");

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lookup the string resource
        String ocrPageTitle = getString(R.string.ocr_page_title);

        // Set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(ocrPageTitle);
        }

//        // Set up the select image button onClickListener
//        Button selectImageButton = findViewById(R.id.select_image_button);
//        selectImageButton.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_ocr;
    }

    /*
     * Process the image URI which is selected from the gallery
     */
    private void processImageUri(Uri uri) {

        Log.d(TAG, "Processing image: " + uri);

        try {

            // read image from uri
            InputImage image = InputImage.fromFilePath(this, uri);

            // Process the image using ML Kit
            //   - Use the processImageWithMLKit() method to process the image
            //   - Use the TextRecognitionCallback to handle the recognized text
            processImageWithMLKit(image, new TextRecognitionCallback() {
                @Override
                public void onTextRecognized(String recognizedText) {

                    // Handle the recognized text here
                    Log.d(TAG, "Extracted Text: " + recognizedText);

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
                    Log.e(TAG, "Error recognizing text", e);
                }
            });
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /*
     * Process the image using ML Kit
     */
    private void processImageWithMLKit(InputImage image, TextRecognitionCallback callback) {

        // Create a TextRecognizer instance
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Process the image using the TextRecognizer
        recognizer.process(image)
            .addOnSuccessListener(text -> {
                // Process recognized text
                String recognizedText = text.getText();
                Log.d(TAG, "Recognized Text: " + recognizedText);
                callback.onTextRecognized(recognizedText); // Use the callback to return the text
            })
            .addOnFailureListener(e -> {
                // Handle failure
                Log.e(TAG, "Text recognition failed", e);
                callback.onError(e); // Use the callback to return the error
            });
    }

    /*
     * Callback interface for handling text recognition
     */
    private @NonNull ImageCapture.OutputFileOptions getOutputFileOptions(String timeStamp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_" + timeStamp + "_");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        return new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();
    }
}
