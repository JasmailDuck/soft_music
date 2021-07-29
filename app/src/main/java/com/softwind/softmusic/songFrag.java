package com.softwind.softmusic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class songFrag extends Fragment implements SongRecycler.ItemClickListener  {

    //Stores the adapter
    private SongRecycler adapter;

    private RecyclerView recyclerView;

    //Stores the art and name of the some in their respective lists.
    private List<Bitmap> image = new ArrayList<Bitmap>();
    private List<String> text = new ArrayList<String>();


    public songFrag() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        text.add("Test Song");
        image.add(BitmapFactory.decodeResource(getResources(), R.drawable.d));
        text.add("Test Song");
        image.add(BitmapFactory.decodeResource(getResources(), R.drawable.d));
        text.add("Test Song");
        image.add(BitmapFactory.decodeResource(getResources(), R.drawable.d));
        text.add("Test Song");
        image.add(BitmapFactory.decodeResource(getResources(), R.drawable.d));
        text.add("Test Song");
        image.add(BitmapFactory.decodeResource(getResources(), R.drawable.d));


        View view = inflater.inflate(R.layout.fragment_song_list_page, container, false);
        recyclerView = view.findViewById(R.id.song_recycler);

        LinearLayoutManager verticalManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(verticalManager);

        adapter = new SongRecycler(getContext(),image,text);
        adapter.setClickListener(this);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();
    }
}