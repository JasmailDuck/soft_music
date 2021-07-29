package com.softwind.softmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.MotionScene;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.makeramen.roundedimageview.RoundedImageView;

public class MainActivity extends AppCompatActivity {

    //Holds the main screen fragment view
    private final HomeContent homeFrag = new HomeContent();

    //Holds the imageview of the album art in the music player
    private RoundedImageView album;


    /**
     * Simple method to control the fragment view
     * @param fragment
     */
    private void fragmentManger(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.home_content_fragment_container, fragment).commitNow();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the views
        album = findViewById(R.id.album_art_player);



        fragmentManger(homeFrag);


    }

}