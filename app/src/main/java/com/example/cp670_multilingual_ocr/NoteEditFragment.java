package com.example.cp670_multilingual_ocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NoteEditFragment extends Fragment {
    NotesList nl;

    public NoteEditFragment(NotesList nl) {
        this.nl = nl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.note_edit_fragment, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // -- Populate frame logic:

        EditText titleEdt = getView().findViewById(R.id.noteTitleEdit);
        EditText noteEdt = getView().findViewById(R.id.noteNoteEdit);

        // -- Add button logic:

        Button addBtn = getView().findViewById(R.id.noteFinalize);
        addBtn.setOnClickListener(v -> {
            String title = titleEdt.getText().toString();
            String note = noteEdt.getText().toString();
            if (nl == null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("title", title);
                resultIntent.putExtra("note", note);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            } else {
                nl.addNote(title, note);
            }
        });
    }
}