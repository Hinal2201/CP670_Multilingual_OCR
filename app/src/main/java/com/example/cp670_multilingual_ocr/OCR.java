package com.example.cp670_multilingual_ocr;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class OCR extends MainActivity {
    private static final String TAG = "OCR";
    private static final int PICK_IMAGE_REQUEST = 2;
    private SQLiteDatabase database;

    private Uri imageUri = null;
    ProgressBar progressBar = null;

    /*
     * Declare the ActivityResultLauncher
     * Remarks: can move this private class attributes orcActivityResultLauncher to other activity class
     */
    private final ActivityResultLauncher<Intent> cameraXActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            progressBar.setVisibility(View.GONE);
            onReceiveCameraXCallback(result.getResultCode(), result.getData());
        }
    );

    /*  
     * Define an ActivityResultLauncher for selecting an image from the gallery at the class level
     * This launcher will be used to select an image from the gallery
     */
    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        progressBar.setVisibility(View.GONE);

        if (uri != null) {
            // TODO: Use MLKit or other OCR library to process the selected image
            Log.d(TAG, "Image selected: " + uri);

            imageUri = uri;
            updateImageView(imageUri);

        }
        else {
            Log.e(TAG, "No image selected");

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

        progressBar = findViewById(R.id.progressBar);

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
        imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.image_placeholder);
        updateImageView(imageUri);

        // Set up the Take Picture button onClickListener
        Button btnTakePicture = findViewById(R.id.btnOcrTakePicture);
        btnTakePicture.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent intent_ocr = new Intent(this, CameraXActivity.class);
            cameraXActivityResultLauncher.launch(intent_ocr); // callback onReceiveCameraXCallback will be trigger once finished
        });

        // Set up the From Gallery button onClickListener
        Button btnFromGallery = findViewById(R.id.btnOcrFromGallery);
        btnFromGallery.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            selectImageLauncher.launch("image/*");
        });

        LinearLayout textRecognitionContainer = findViewById(R.id.textRecognitionContainer);
        textRecognitionContainer.setVisibility(View.GONE);

        LinearLayout multilineEditTextContainer = findViewById(R.id.multilineEditTextContainer);
        multilineEditTextContainer.setVisibility(View.GONE);

        Button btnTextRecognition = findViewById(R.id.btnTextRecognition);
        btnTextRecognition.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            onTextRecognitionButtonClicked(v);
        });

        LinearLayout btnAddNoteContainer = findViewById(R.id.btnAddNoteContainer);
        btnAddNoteContainer.setVisibility(View.GONE);

        Button btnAddNote = findViewById(R.id.btnAddNote);
        btnAddNote.setOnClickListener(v -> {
            EditText ocrNoteTitleEditText = findViewById(R.id.ocrNoteTitleEditText);
            EditText ocrNoteDetailEditText = findViewById(R.id.ocrNoteDetailEditText);
            String title = ocrNoteTitleEditText.getText().toString();
            String note = ocrNoteDetailEditText.getText().toString();
            addNote(title, note);
        });

        NoteDatabaseHelper dbHelper = new NoteDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_ocr;
    }

    /*
     * Method to handle the result from cameraXActivityResultLauncher
     */
    private void onReceiveCameraXCallback(int resultCode, Intent data) {

        if (resultCode == RESULT_OK && data != null) {
            // Get imageUri from data intent
            String imageUriString = data.getStringExtra("imageUri");
            Log.d(TAG, "Image URI received: " + imageUriString);

            imageUri = Uri.parse(imageUriString);
            Log.d(TAG, "Image URI parsed: " + imageUri);

            if (imageUri != null) {
                Log.d(TAG, "ImageUri received from CameraXActivity");
                updateImageView(imageUri);
            } else {
                Log.e(TAG, "No image received from CameraXActivity");
            }
        } else {
            Log.d(TAG, "Error in CameraXActivity");
        }
    }

    /*
     * onTextRecognitionButtonClicked is called when the user clicks the Text Recognition button
     */
    private void onTextRecognitionButtonClicked(View v) {
        Log.d(TAG, "Text Recognition button clicked");
        // Get imageUri from imagePlaceholder
        ImageView imagePlaceholder = findViewById(R.id.imagePlaceholder);
        processImageUri(imageUri);
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
                    Log.d("MLKit", "Extracted Text: " + recognizedText);

                    progressBar.setVisibility(View.GONE);

                    // Check if length of recognizedText > 0
                    if (recognizedText.isEmpty()) {
                        Log.e("TAG", "Extracted Text is empty");
                        // Create a toast message to inform the user that no text was recognized
                        Toast.makeText(OCR.this, getString(R.string.toast_no_text_extracted), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "Extracted Text: " + recognizedText);

                    // set extracted text to ocrNoteDetailEditText
                    Log.d(TAG, "Setting extracted text to ocrNoteDetailEditText");
                    EditText ocrNoteDetailEditText = findViewById(R.id.ocrNoteDetailEditText);
                    ocrNoteDetailEditText.setText(recognizedText);

                }
            
                @Override
                public void onError(Exception e) {
                    // Handle errors here
                    Log.e("MLKit", "onError: ", e);
                    progressBar.setVisibility(View.GONE);
                }
            });
            
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
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
        // TODO: how to set progress bar?
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
    private void updateImageView(Uri uri) {
        Log.d(TAG, "Updating image view");

        ImageView imagePlaceholder = findViewById(R.id.imagePlaceholder);
        Log.d(TAG, "Image placeholder: " + imagePlaceholder);

        Bitmap bitmap = null;

        // Extract bitmap from the image URI
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
            return;
        }

        // Log bitmap extracted
        Log.d(TAG, "Bitmap extracted");

        // Set the image view bitmap
        Log.d(TAG, "Setting ImageView bitmap");
        imagePlaceholder.setImageBitmap(bitmap);

        // Set the image view scale type to adjust the bounds
        imagePlaceholder.setAdjustViewBounds(true);

        // Change btnOcrTakePicture button from primary button to outlined button
        Button btnTakePicture = findViewById(R.id.btnOcrTakePicture);


        // Set textRecognitionContainer layout visible
        LinearLayout textRecognitionContainer = findViewById(R.id.textRecognitionContainer);
        textRecognitionContainer.setVisibility(View.VISIBLE);

        LinearLayout multilineEditTextContainer = findViewById(R.id.multilineEditTextContainer);
        multilineEditTextContainer.setVisibility(View.VISIBLE);

        LinearLayout btnAddNoteContainer = findViewById(R.id.btnAddNoteContainer);
        btnAddNoteContainer.setVisibility(View.VISIBLE);

    }

    public void addNote(String title, String note) {

        if (title.isEmpty()) {
            Toast toast = Toast.makeText(this, getString(R.string.toast_title_cannot_be_empty), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (note.isEmpty()) {
            Toast toast = Toast.makeText(this, getString(R.string.toast_note_detail_cannot_be_empty), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(NoteDatabaseHelper.KEY_TITLE, title);
        values.put(NoteDatabaseHelper.KEY_NOTE, note);
        long result = database.insert(NoteDatabaseHelper.TABLE_NAME, null, values);
        if (result > 0) {
            Log.i(TAG, "Note titled: " + title + " added");
            Toast toast = Toast.makeText(this, getString(R.string.word_note) + " " + title + " " + getString(R.string.word_added), Toast.LENGTH_SHORT);
            toast.show();

            // Clear title, imagePlaceholder, and textRecognitionContainer
            EditText ocrNoteTitleEditText = findViewById(R.id.ocrNoteTitleEditText);
            ocrNoteTitleEditText.setText("");

            ImageView imagePlaceholder = findViewById(R.id.imagePlaceholder);
            imagePlaceholder.setImageResource(R.drawable.image_placeholder);

            EditText ocrNoteDetailEditText = findViewById(R.id.ocrNoteDetailEditText);
            ocrNoteDetailEditText.setText("");

            LinearLayout textRecognitionContainer = findViewById(R.id.textRecognitionContainer);
            textRecognitionContainer.setVisibility(View.GONE);

            LinearLayout multilineEditTextContainer = findViewById(R.id.multilineEditTextContainer);
            multilineEditTextContainer.setVisibility(View.GONE);

            LinearLayout btnAddNoteContainer = findViewById(R.id.btnAddNoteContainer);
            btnAddNoteContainer.setVisibility(View.GONE);

        } else {
            Log.i(TAG, "Note titled: " + title + " failed to add");
            Toast toast = Toast.makeText(this, getString(R.string.word_note) + " " + title + " " + getString(R.string.word_failed_to_add), Toast.LENGTH_SHORT);
            toast.show();
        }
    }


}
