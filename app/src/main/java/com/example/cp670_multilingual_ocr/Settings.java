package com.example.cp670_multilingual_ocr;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Objects;

public class Settings extends MainActivity {
    private static final String TAG = "Settings";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "inside onCreate");

        super.onCreate(savedInstanceState);

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

                    TranslateDb t = new TranslateDb();

                    if (selectedLanguage.equals("English")) {
                        LocaleHelper.setLocale(Settings.this, "en");
                        t.execute("en");
                    } else {
                        LocaleHelper.setLocale(Settings.this, "es");
                        t.execute("es");
                    }

                    //Refresh UI to apply new language
                    recreate();
                    Intent intent = new Intent(Settings.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
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

    private class TranslateDb extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            NoteDatabaseHelper dbHelper = new NoteDatabaseHelper(Settings.this);
            SQLiteDatabase database = dbHelper.getWritableDatabase();

            TranslatorOptions options = null;

            //If we select english, then english is the Target lang, else spanish
            if(Objects.equals(strings[0], "en")) {
                options = new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.SPANISH)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build();
            } else if (Objects.equals(strings[0], "es"))
            {
                options = new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.SPANISH)
                        .build();
            }

            Cursor c = database.rawQuery("SELECT * from "+ NoteDatabaseHelper.TABLE_NAME, new String[] {});
            int noteColIndex = c.getColumnIndex(NoteDatabaseHelper.KEY_NOTE);
            c.moveToFirst();

            final Translator myTranslator = Translation.getClient(options);
            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()
                    .build();

            //We run this task synchronously as we are already inside async, and rest of code needs this to be complete
            Task<Void> d = myTranslator.downloadModelIfNeeded(conditions);
            while(!d.isComplete()){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            while (!c.isAfterLast() && options != null)
            {
                try {
                    Task<String> translation = myTranslator.translate(c.getString(noteColIndex));
                    while(!translation.isComplete()){
                        Thread.sleep(500);
                    }
                    String translatedText = translation.getResult();
                    ContentValues values = new ContentValues();
                    values.put(NoteDatabaseHelper.KEY_NOTE, translatedText);
                    @SuppressLint("Range")
                    String id = String.valueOf(c.getInt(c.getColumnIndex(NoteDatabaseHelper.KEY_ID)));
                    database.update(NoteDatabaseHelper.TABLE_NAME, values, "id = "+id, new String[]{});
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                c.moveToNext();
            }

            c.close();
            database.close();
            Settings.this.finish();
            return null;
        }
    }

}