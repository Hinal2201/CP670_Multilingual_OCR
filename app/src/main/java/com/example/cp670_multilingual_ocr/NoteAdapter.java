package com.example.cp670_multilingual_ocr;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private static final String TAG = "NoteAdapter";

    private final ArrayList<String> localDataSet;
    private final OnItemClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Button button;

        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);

            button = view.findViewById(R.id.noteTitleRecycler);
            button.setOnClickListener(v -> {
                int pos = getAdapterPosition();

                if (pos != RecyclerView.NO_POSITION){
                    listener.onItemClick(pos);
                }
            });
        }

        public Button getButton() {
            return button;
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
        viewHolder.getButton().setText(localDataSet.get(position));
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
