package com.example.cp670_multilingual_ocr;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

public class NoteDetails extends MainActivity {
    private static final String TAG = "NoteDetails";

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


        // -- Load frame logic:

        String pos = getIntent().getStringExtra("pos");
        String id = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        String note = getIntent().getStringExtra("note");

        Bundle bundle = new Bundle();
        bundle.putString("pos", pos);
        bundle.putString("id", id);
        bundle.putString("title", title);
        bundle.putString("note", note);

        NoteFragment frag = new NoteFragment(null);
        frag.setArguments(bundle);

        FragmentTransaction ft =
                getSupportFragmentManager().beginTransaction();

        ft.add(R.id.noteNoteInNewWindow, frag);
        ft.commit();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_note_details;
    }
}