package com.example.cp670_multilingual_ocr;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


public class OCR extends MainActivity {
    private static final String TAG = "OCRActivity";
    private static final int PICK_IMAGE_REQUEST = 2;

    /*
     * Declare the ActivityResultLauncher
     * Remarks: can move this private class attributes orcActivityResultLauncher to other activity class
     */
    private final ActivityResultLauncher<Intent> cameraXActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            onReceiveCameraXCallback(result.getResultCode(), result.getData());
        }
    );

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

        // get Uri of res/drawable/image_placeholder.png
        Uri imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.image_placeholder);
        resizeImageView(imageUri);

        // Set up the Take Picture button onClickListener
        Button btnTakePicture = findViewById(R.id.btnOcrTakePicture);
        btnTakePicture.setOnClickListener(v -> {
            Intent intent_ocr = new Intent(this, CameraXActivity.class);
            cameraXActivityResultLauncher.launch(intent_ocr); // callback onReceiveCameraXCallback will be trigger once finished
        });

        // Set up the From Gallery button onClickListener
        Button btnFromGallery = findViewById(R.id.btnOcrFromGallery);
        btnFromGallery.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_ocr;
    }

    /*
     * Remarks: onActivityResult can be clone in other activity class if calling OCR activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result comes from the OCR activity
        if (requestCode == OCR_REQUEST_CODE) {
            onReceiveOcrCallback(resultCode, data);
        }
    }

    /*
     * Method to handle the result from the OCR activity
     * Remarks: onReceiveOcrCallback can be clone in other activity class if calling OCR activity
     */
    private void onReceiveCameraXCallback(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
//            // Extract the recognized text from the Intent
//            String recognizedText = data.getStringExtra("recognizedText");
//
//            // Use the recognized text here
//            Log.d("MainActivity", "Received recognized text: " + recognizedText);
//            // For example, update a TextView
//            // textView.setText(recognizedText);
        } else {
            Log.d("MainActivity", "No recognized text received");
        }
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
     * Callback interface for handling MLKit text recognition
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

    /*
     * Resize the image view based on the aspect ratio of the image
     */
    private void resizeImageView(Uri imageUri) {
        ImageView imagePlaceholder = findViewById(R.id.imagePlaceholder);
        int desiredWidth = getResources().getDisplayMetrics().widthPixels;

        float aspectRatio = getImageAspectRatio(imageUri);
        Log.d(TAG, "Aspect Ratio: " + aspectRatio);

        int desiredHeight = (int) (desiredWidth * aspectRatio); // Calculate the height based on the desired width and aspect ratio
        imagePlaceholder.getLayoutParams().width = desiredWidth;
        imagePlaceholder.getLayoutParams().height = desiredHeight;
        imagePlaceholder.requestLayout();

    }

    /*
     * Get the aspect ratio of the image
     */
    private float getImageAspectRatio(Uri imageUri) {
        try {
            // Decode the image dimensions without loading the full image into memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);
            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;

            return (float) imageWidth / imageHeight;
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            return 0; // Return a default value or handle the error as per your requirement
        }
    }
}
