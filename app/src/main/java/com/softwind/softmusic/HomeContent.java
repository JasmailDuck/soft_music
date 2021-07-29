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
 * Use the {@link HomeContent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeContent extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    LayoutInflater inflater;
    ViewGroup container;
    Bundle savedInstanceState;

    ImageButton songButton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private com.softwind.softmusic.songFrag songFrag = new songFrag();

    public HomeContent() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home_content.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeContent newInstance(String param1, String param2) {
        HomeContent fragment = new HomeContent();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }


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