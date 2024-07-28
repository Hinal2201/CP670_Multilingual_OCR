package com.example.cp670_multilingual_ocr;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NotesList extends MainActivity implements NoteAdapter.OnItemClickListener {
    private static final String TAG = "NotesList";
    private SQLiteDatabase database;
    private ArrayList<String> noteTitles;
    private HashMap<Integer, ArrayList<String>> posToIdnNote;
    private Cursor cursor;
    private NoteAdapter noteAdapter;
    FrameLayout fl;
    private static final Integer LAUNCH_NOTE_DETAILS = 10;
    private static final Integer LAUNCH_NOTE_ADD = 11;

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

        // Get database reference:

        NoteDatabaseHelper dbHelper = new NoteDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        // -- Fill up note data:

        noteTitles = new ArrayList<>();
        posToIdnNote = new HashMap<>();
        loadNotes(true);

        // -- Get frame layout reference

        fl = findViewById(R.id.noteNote);

        // -- Fill out the recycler view:

        noteAdapter = new NoteAdapter(noteTitles, this);
        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new GridLayoutManager(this, calculateNoOfColumns(this, fl != null)));
        recyclerView.setAdapter(noteAdapter);

        // -- Note add

        Button noteAddBtn = findViewById(R.id.noteAdd);
        noteAddBtn.setOnClickListener(v -> onNoteAddClick());
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_notes_list;
    }

    public static int calculateNoOfColumns(Context context, boolean isMultiLayout) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        if (isMultiLayout) {
            return 2;
        }
        return (int) (dpWidth / 180);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);

        if (requestCode == LAUNCH_NOTE_DETAILS && responseCode == RESULT_OK) {
            Integer pos = Integer.valueOf(data.getStringExtra("pos"));
            String id = data.getStringExtra("id");
            deleteNote(id, pos);
        }

        if (requestCode == LAUNCH_NOTE_ADD && responseCode == RESULT_OK) {
            String title = data.getStringExtra("title");
            String note = data.getStringExtra("note");
            addNote(title, note);
        }
    }

    public void deleteNote(String id, Integer pos) {
        database.delete(NoteDatabaseHelper.TABLE_NAME, NoteDatabaseHelper.KEY_ID + "=?", new String[]{id});

        String removedNoteTitle = noteTitles.get(pos);
        removedNoteTitle = removedNoteTitle.length() > 8 ? removedNoteTitle.substring(0, 8) + "..." : removedNoteTitle;
        noteTitles.remove(pos);

        loadNotes(false);
        noteAdapter.deleteItem(pos);
        removeFragmentIfExists(R.id.noteNote);
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), "Note titled: " + removedNoteTitle + " deleted!", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void addNote(String title, String note) {
        ContentValues values = new ContentValues();
        values.put(NoteDatabaseHelper.KEY_TITLE, title);
        values.put(NoteDatabaseHelper.KEY_NOTE, note);
        database.insert(NoteDatabaseHelper.TABLE_NAME, null, values);

        loadNotes(false);
        noteAdapter.insertItem(title);
        removeFragmentIfExists(R.id.noteNote);
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), "Note titled: " + title + " added", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void removeFragmentIfExists(int id) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(id);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    public void loadNotes(boolean isInitializing) {
        cursor = database.query(
                NoteDatabaseHelper.TABLE_NAME, null, null, null, null,
                null, null);
        cursor.moveToFirst();

        int i = 0;
        while (!cursor.isAfterLast()) {
            int idIdx = cursor.getColumnIndex(NoteDatabaseHelper.KEY_ID);
            String id = cursor.getString(idIdx);

            int titleIdx = cursor.getColumnIndex(NoteDatabaseHelper.KEY_TITLE);
            String title = cursor.getString(titleIdx);

            int noteIdx = cursor.getColumnIndex(NoteDatabaseHelper.KEY_NOTE);
            String note = cursor.getString(noteIdx);

            if (isInitializing) {
                noteTitles.add(title);
            }
            ArrayList<String> noteFull = new ArrayList<>(Arrays.asList(id, note));
            posToIdnNote.put(i, noteFull);

            cursor.moveToNext();
            i++;
        }
    }

    public void onNoteAddClick() {
        if (fl != null) {
            removeFragmentIfExists(R.id.noteNote);
            NoteEditFragment frag = new NoteEditFragment(this);

            FragmentTransaction ft =
                    getSupportFragmentManager().beginTransaction();
            ft.add(R.id.noteNote, frag);

            ft.commit();
        } else {
            Intent resultIntent = new Intent(NotesList.this, NoteDetails.class);
            resultIntent.putExtra("pos", "null");
            startActivityForResult(resultIntent, LAUNCH_NOTE_ADD);
        }
    }

    // -- Recycler note item click listener
    @Override
    public void onItemClick(int position) {
        ArrayList<String> cache = posToIdnNote.get(position);
        String id = cache.get(0);
        String title = noteTitles.get(position);
        String note = cache.get(1);

        Log.i(TAG, "item " + position + " clicked");

        if (fl != null) {
            Bundle bundle = new Bundle();
            bundle.putString("pos", String.valueOf(position));
            bundle.putString("id", String.valueOf(id));
            bundle.putString("title", title);
            bundle.putString("note", note);

            removeFragmentIfExists(R.id.noteNote);
            NoteFragment frag = new NoteFragment(this);
            frag.setArguments(bundle);

            FragmentTransaction ft =
                    getSupportFragmentManager().beginTransaction();

            ft.add(R.id.noteNote, frag);
            ft.commit();
        } else {
            Intent resultIntent = new Intent(NotesList.this, NoteDetails.class);
            resultIntent.putExtra("pos", String.valueOf(position));
            resultIntent.putExtra("id", String.valueOf(id));
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("note", note);
            startActivityForResult(resultIntent, LAUNCH_NOTE_DETAILS);
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "inside onDestroy");
        super.onDestroy();
        database.close();
        cursor.close();
    }
}
