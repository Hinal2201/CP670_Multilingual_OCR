package com.example.cp670_multilingual_ocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Declare the ActivityResultLauncher
    /*
     * Remarks: can move this private class attributes orcActivityResultLauncher to other activity class
     */
    private final ActivityResultLauncher<Intent> ocrActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            onReceiveOcrCallback(result.getResultCode(), result.getData());
        }
    );
    
    // Define a unique request code for the OCR activity
    private static final int OCR_REQUEST_CODE = 1234; // Example unique request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "inside onCreate");

        setContentView(R.layout.activity_main);

        // Button startOcrButton = findViewById(R.id.start_ocr_button);
        // startOcrButton.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         // Start the OCR activity
        //         startOcrActivity();
        //     }
        // });

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected int getLayoutResource(){
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();

        if (item_id == R.id.action_home) {
            Log.i(TAG, "User clicked Home");
            Intent intent_home = new Intent(this, MainActivity.class);
            startActivity(intent_home);
        }
        else if (item_id == R.id.action_ocr) {
            Log.i(TAG, "User clicked OCR");
//            Intent intent_ocr = new Intent(MainActivity.this, OCR.class);
//            startActivity(intent_ocr);
            startOcrActivity();
        }
        else if (item_id == R.id.action_notes) {
            Log.i(TAG, "User clicked Notes");
            Intent intent_notes = new Intent(this, NotesList.class);
            startActivity(intent_notes);
        }
        else if (item_id == R.id.action_settings) {
            Log.i(TAG, "User clicked Settings");
            Intent intent_settings = new Intent(this, Settings.class);
            startActivity(intent_settings);
        }
        else if (item_id == R.id.action_help) {
            Log.i(TAG, "User clicked Help");
            Intent intent_help = new Intent(this, Help.class);
            startActivity(intent_help);
        }
        else if (item_id == android.R.id.home){
            Log.i(TAG, "User clicked Back");
            onBackPressed();
        }

        return true;
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

    // Method to start the OCR activity
    /*
     * Remarks: startOcrActivity can be clone in other activity class if calling OCR activity
     */
    private void startOcrActivity() {
        Intent intent = new Intent(MainActivity.this, OCR.class);
        ocrActivityResultLauncher.launch(intent);
    }

    // Method to handle the result from the OCR activity
    /*
     * Remarks: onReceiveOcrCallback can be clone in other activity class if calling OCR activity
     */
    private void onReceiveOcrCallback(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            // Extract the recognized text from the Intent
            String recognizedText = data.getStringExtra("recognizedText");

            // Use the recognized text here
            Log.d("MainActivity", "Received recognized text: " + recognizedText);
            // For example, update a TextView
            // textView.setText(recognizedText);
        } else {
            Log.d("MainActivity", "No recognized text received");
        }
    }

}