package com.softwind.softmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongRecycler extends RecyclerView.Adapter<SongRecycler.ViewHolder> {

    private List<Bitmap> songImage;
    private List<String> songName;

    private LayoutInflater mInflater;

    private ItemClickListener mClickListener;

    // data is passed into the constructor
    SongRecycler(Context context, List<Bitmap> art, List<String> name) {
        this.mInflater = LayoutInflater.from(context);
        this.songImage = art;
        this.songName = name;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.songlistview, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bitmap art = songImage.get(position);
        String text = songName.get(position);
        holder.myView.setImageBitmap(art);
        holder.myTextView.setText(text);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return songName.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView myView;
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myView = itemView.findViewById(R.id.songImage);
            myTextView = itemView.findViewById(R.id.songName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return songName.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
