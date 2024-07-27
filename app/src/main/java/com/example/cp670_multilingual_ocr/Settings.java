package com.example.cp670_multilingual_ocr;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Settings extends MainActivity {
    private static final String TAG = "Settings";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "inside onCreate");

        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Spinner languageSpinner = findViewById(R.id.spinner);
        // Set up the adapter for the Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_settings_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                String currentLanguage = LocaleHelper.getLanguage(Settings.this);

                // Check if the selected language is different from the current language
                if ((selectedLanguage.equals("English") && !currentLanguage.equals("en")) ||
                        (selectedLanguage.equals("Español") && !currentLanguage.equals("es"))) {

                    if (selectedLanguage.equals("English")) {
                        LocaleHelper.setLocale(Settings.this, "en");
                    } else if (selectedLanguage.equals("Español")) {
                        LocaleHelper.setLocale(Settings.this, "es");
                    }

                    // Refresh UI to apply new language
                    recreate();
                    Intent intent = new Intent(Settings.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    // Set the spinner to the currently selected language
    String currentLanguage = LocaleHelper.getLanguage(this);
        if (currentLanguage.equals("en")) {
        languageSpinner.setSelection(0);
    } else if (currentLanguage.equals("es")) {
        languageSpinner.setSelection(1);
    }
}
    @Override
    public int getLayoutResource(){
        return R.layout.activity_settings;
    }

}