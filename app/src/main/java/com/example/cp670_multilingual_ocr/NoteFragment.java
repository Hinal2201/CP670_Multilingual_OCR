package com.example.cp670_multilingual_ocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NoteFragment extends Fragment {
    NotesList nl;

    public NoteFragment(NotesList nl){ this.nl = nl;}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.note_fragment, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String pos = getArguments().getString("pos");
            String id = getArguments().getString("id");
            String title = getArguments().getString("title");
            String note = getArguments().getString("note");

            // -- Populate frame logic:

            TextView titleTxt = getView().findViewById(R.id.noteTitle);
            titleTxt.setText(title);
            TextView noteTxt = getView().findViewById(R.id.noteNote);
            noteTxt.setText(String.format(note));

            // -- Delete button logic:

            Button deleteBtn = getView().findViewById(R.id.noteDelete);
            deleteBtn.setOnClickListener(v -> {
                if (nl == null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("pos", pos);
                    resultIntent.putExtra("id", id);
                    getActivity().setResult(Activity.RESULT_OK, resultIntent);
                    getActivity().finish();
                } else {
                    nl.deleteNote(id, Integer.valueOf(pos));
                }
            });
        }
    }
}