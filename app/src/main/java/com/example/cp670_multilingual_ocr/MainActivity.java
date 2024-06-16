package com.example.cp670_multilingual_ocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Define a unique request code for the OCR activity
    private static final int OCR_REQUEST_CODE = 1234; // Example unique request code
    private ActivityResultLauncher<Intent> ocrActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "inside onCreate");

        setContentView(R.layout.activity_main);

        ocrActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Handle the result from the OCR activity
                    Intent data = result.getData();
                    Log.i("MainActivity", "Received recognized text: " + data.getStringExtra("recognizedText"));
                }
            }
        );

        Button startOcrButton = findViewById(R.id.start_ocr_button);
        startOcrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OCR.class);
                ocrActivityResultLauncher.launch(intent);
            }
        });

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    
        // Check if the result comes from the OCR activity
        if (requestCode == OCR_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                // Extract the recognized text from the Intent
                String recognizedText = data.getStringExtra("recognizedText");

                // Use the recognized text here
                Log.d("YourActivity", "Received recognized text: " + recognizedText);
                // For example, update a TextView
                // textView.setText(recognizedText);
            } else {
                Log.d("YourActivity", "No recognized text received");
            }
        }
    }
}