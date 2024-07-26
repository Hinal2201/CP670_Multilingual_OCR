package com.example.cp670_multilingual_ocr;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;

public class Help extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Help");
        }

        // Set authors' names, app version, and usage instructions
        TextView authorsTextView = findViewById(R.id.authors);
        TextView versionTextView = findViewById(R.id.version);
        TextView instructionsTextView = findViewById(R.id.instructions);

        authorsTextView.setText(R.string.authors);
        versionTextView.setText(R.string.version);
        instructionsTextView.setText(R.string.usage_instructions);

    }


    @Override
    public int getLayoutResource(){
        return R.layout.activity_help;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}