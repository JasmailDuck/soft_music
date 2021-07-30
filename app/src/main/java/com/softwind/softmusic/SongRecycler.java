package com.softwind.softmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Addapter class and Recycle view inflater
 * @author jasmailduck
 * @version 1.0
 */
public class SongRecycler extends RecyclerView.Adapter<SongRecycler.ViewHolder> {

    //Stores the respective lists of data that the inflater will bind together
    private List<Bitmap> songImage;
    private List<String> songName;

    //Stores the layout inflater
    private LayoutInflater mInflater;

    //Stores the onItemClick listener
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    SongRecycler(Context context, List<Bitmap> art, List<String> name) {
        this.mInflater = LayoutInflater.from(context);
        this.songImage = art;
        this.songName = name;
        Toast.makeText(mInflater.getContext(), "There are" + art.size(),Toast.LENGTH_SHORT).show();
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.songlistview, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the imageview and textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bitmap art = songImage.get(position);
        String text = songName.get(position);

        loadBitmapByPicasso(mInflater.getContext(),art,holder.myView);
        holder.myTextView.setText(text);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return songName.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView myView;
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



    private void loadBitmapByPicasso(Context pContext, Bitmap pBitmap, RoundedImageView pImageView) {
        try {

            Uri uri = Uri.fromFile(File.createTempFile("temp_file_name", ".jpg", pContext.getCacheDir()));
            OutputStream outputStream = pContext.getContentResolver().openOutputStream(uri);
            pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Picasso.get().load(uri).fit().placeholder(R.drawable.ic_launcher_background).into(pImageView);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.missing_art).fit().into(pImageView);
        }
    }
}
