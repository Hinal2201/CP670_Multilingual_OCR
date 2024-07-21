package com.example.cp670_multilingual_ocr;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotesList extends MainActivity {
    private static final String TAG = "NotesList";

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


        NoteDatabaseHelper dbHelper = new NoteDatabaseHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // -- Fill out note titles:

        ArrayList<String> noteTitles = new ArrayList<>();

        Cursor cursor = database.query(
                NoteDatabaseHelper.TABLE_NAME, null, null, null, null,
                null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int titleIdx = cursor.getColumnIndex(NoteDatabaseHelper.KEY_TITLE);
            String title = cursor.getString(titleIdx);

            int noteIdx = cursor.getColumnIndex(NoteDatabaseHelper.KEY_NOTE);
            String note = cursor.getString(noteIdx);

            noteTitles.add(title);

            cursor.moveToNext();
        }

        // -- fill out the recycler view:

        NoteAdapter noteAdapter = new NoteAdapter(noteTitles);
        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new GridLayoutManager(this, calculateNoOfColumns(this)));
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    public int getLayoutResource(){
        return R.layout.activity_notes_list;
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 180);
    }
}