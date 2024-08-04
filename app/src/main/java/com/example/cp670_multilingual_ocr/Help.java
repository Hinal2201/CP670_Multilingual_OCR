package com.example.cp670_multilingual_ocr;

import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Help extends MainActivity {
    private ExpandableListView expandableListView;
    private HelpExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, HelpItem> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.Help));
        }
        TextView authorsTextView = findViewById(R.id.authorsTextView);
        TextView versionTextView = findViewById(R.id.versionTextView);
        TextView instructionsTextView = findViewById(R.id.instructionsTextView);

        authorsTextView.setText(getString(R.string.authors));
        versionTextView.setText(getString(R.string.version));
        instructionsTextView.setText(getString(R.string.usage_instructions));

        expandableListView = findViewById(R.id.expandableListView);
        prepareListData();

        listAdapter = new HelpExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(listAdapter);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_help;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        listDataHeader.add(getString(R.string.help_ocr));
        listDataHeader.add(getString(R.string.help_notes));
        listDataHeader.add(getString(R.string.help_settings));
        listDataHeader.add(getString(R.string.help_assistance));

        HelpItem ocrItem = new HelpItem(getString(R.string.ocr_instructions), R.drawable.ocr_sc);
        HelpItem notesItem = new HelpItem(getString(R.string.note_instructions),R.drawable.notes_sc);
        HelpItem settingsItem = new HelpItem(getString(R.string.settings_help_details), R.drawable.settings_sc);
        HelpItem helpItem = new HelpItem(getString(R.string.help_assistance_details), 0);

        listDataChild.put(listDataHeader.get(0), ocrItem);
        listDataChild.put(listDataHeader.get(1), notesItem);
        listDataChild.put(listDataHeader.get(2), settingsItem);
        listDataChild.put(listDataHeader.get(3), helpItem);
    }
}