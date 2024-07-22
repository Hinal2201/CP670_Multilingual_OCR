package com.example.cp670_multilingual_ocr;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private static final String TAG = "NoteAdapter";

    private ArrayList<String> localDataSet;
    private final OnItemClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);

            textView = view.findViewById(R.id.noteTitleRecycler);
            textView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();

                if (pos != RecyclerView.NO_POSITION){
                    listener.onItemClick(pos);
                }
            });
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public NoteAdapter(ArrayList<String> dataSet, OnItemClickListener listener) {
        Log.i(TAG, "creating it");
        localDataSet = dataSet;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.note_item, viewGroup, false);

        return new NoteAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(NoteAdapter.ViewHolder viewHolder, final int position) {
        Log.i(TAG, "" + position);
        viewHolder.getTextView().setText(localDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void deleteItem(int position){
        localDataSet.remove(position);
        notifyItemRemoved(position);
    }
}
