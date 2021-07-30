package com.softwind.softmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class
 * @author jasmailduck
 *
 */
public class MainActivity extends AppCompatActivity{

    //-------DATA-----------//

    //Stores song information to be converted to a Song object.
    private String songName;
    private String albumName;
    private String artistName;
    private String genreName;

    //Stores the song object.
    private Song song;

    //Stores the song art in bitmap format.
    private Bitmap songImage;

    //Stores the paths for the songs found
    private ArrayList<String> listOfSongPaths;

    //Stores the art and name of the some in their respective lists.
    private List<Bitmap> image = new ArrayList<Bitmap>();
    private List<String> text = new ArrayList<String>();

    //Stores the metadata retriever.
    private MediaMetadataRetriever metadataRetriever;

    //Stores the byte array needed to covert tha song image to a bitmap
    private byte[] art;

    private MediaPlayer mediaPlayer;

    //------------VIEWS & ADAPTERS-------------//

    //Holds the main screen fragment view
    private final HomeContent HOME_FRAG = new HomeContent();

    //Holds the imageview of the album art in the music player
    private RoundedImageView album;

    SeekBar seekBar;

    Runnable runnable;

    Handler handler;

    //--------------------PRIVATE METHODS--------------------//



    /**
     * Simple method to control the fragment view
     * @param fragment
     */
    private void fragmentManger(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.home_content_fragment_container, fragment).commitNow();
    }

    /**
     * Handles android permissions.
     *
     * @// TODO: 2021-07-17 Still needs methods to check if permission is granted and such.
     */
    private void permissionHandler() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 192);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
    }

    /**
     * Method is responsible for finding mp3 files.
     * Uses recursive method calling.
     *
     * @param rootPath
     * @return
     */
    private ArrayList<String> findSongs(String rootPath) {
        ArrayList<String> fileList = new ArrayList<>();
        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (findSongs(file.getAbsolutePath()) != null) {
                        fileList.addAll(findSongs(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    if (fileList.contains(file.getPath())) {

                    } else {
                        fileList.add(file.getPath());
                    }

                }
            }
            return fileList;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the metadata from the song, if there aren't any for some files, a default theme is applied.
     */
    private void metadataExtraction() {
        for (int i = 0; i < listOfSongPaths.size(); i++) {
            metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(this, Uri.fromFile(new File(listOfSongPaths.get(i))));


            try {
                art = metadataRetriever.getEmbeddedPicture();
                songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
                art = null;

            } catch (Exception e) {
                songImage = BitmapFactory.decodeResource(getResources(), R.drawable.missing_art);
            }
            try {
                albumName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            } catch (Exception e) {
                albumName = "Unknown";
            }
            try {
                artistName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            } catch (Exception e) {
                artistName = "Unknown";
            }
            try {
                genreName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            } catch (Exception e) {
                genreName = "Unknown";
            }
            songName = new File(listOfSongPaths.get(i)).getName();
            song = new Song(formatSongName(songName), artistName, albumName, genreName, new File(listOfSongPaths.get(i)), songImage);

            ListOfSongs.listOfSongs.add(song);
        }
    }

    private String formatSongName(String songName) {
        int numDigits = 0;
        String result = "";
        String extensionRemoved = songName.substring(0, songName.lastIndexOf('.'));
        for (int i = 0; i < extensionRemoved.length(); i++) {
            if (Character.isDigit(extensionRemoved.charAt(i))) {
                numDigits = i;
            }
        }
        if (numDigits > 3) {
            for (int i = 0; i < extensionRemoved.length(); i++) {
                if (Character.isAlphabetic(extensionRemoved.charAt(i))) {
                    result = result + extensionRemoved.charAt(i);
                }
            }
            return result;
        }
        return extensionRemoved;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionHandler();

        listOfSongPaths = findSongs(Environment.getExternalStorageDirectory().getAbsolutePath());

        metadataExtraction();

        //Initialize the views
        album = findViewById(R.id.album_art_player);



        fragmentManger(HOME_FRAG);

        album.setImageResource(R.drawable.missing_art);

        handler = new Handler();

        seekBar = findViewById(R.id.music_seeker);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    public void setMediaPlayerInfo(int position){
        loadBitmapByPicasso(this, ListOfSongs.listOfSongs.get(position).getArt(), album);
        Toast.makeText(this,"Imagae No", Toast.LENGTH_SHORT).show();
    }



    private void loadBitmapByPicasso(Context pContext, Bitmap pBitmap, RoundedImageView pImageView) {
        try {

            Uri uri = Uri.fromFile(File.createTempFile("temp_file_name", ".jpg", pContext.getCacheDir()));
            OutputStream outputStream = pContext.getContentResolver().openOutputStream(uri);
            pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Picasso.get().load(uri).fit().placeholder(R.drawable.missing_art).into(pImageView);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.missing_art).fit().into(pImageView);
        }
    }


    public  void playMedia(Context context, int position){
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Uri uri = Uri.fromFile(ListOfSongs.listOfSongs.get(position).getPath());
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(context.getApplicationContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                seekBarUpdater();

            }
        });

    }

    public void seekBarUpdater(){
        int currPos = mediaPlayer.getCurrentPosition();
        seekBar.setProgress(currPos);

        runnable = new Runnable() {
            @Override
            public void run() {
                seekBarUpdater();
            }
        };

        handler.postDelayed(runnable, 1000);

    }
}