package com.softwind.softmusic;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 *
 * create an instance of this fragment.
 */
public class HomeContent extends Fragment implements View.OnClickListener{



    LayoutInflater inflater;
    ViewGroup container;
    Bundle savedInstanceState;

    ImageButton songButton;



    private com.softwind.softmusic.songFrag songFrag = new songFrag();

    public HomeContent() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_content, container, false);
        songButton = view.findViewById(R.id.song_button);
        songButton.setOnClickListener(this);
        // Inflate the layout for this fragment


        return view;
    }

    @Override
    public void onClick(View v) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.home_content_fragment_container,songFrag).addToBackStack(null).commit();
        Toast.makeText(getContext(),"lol",Toast.LENGTH_LONG).show();
    }
}