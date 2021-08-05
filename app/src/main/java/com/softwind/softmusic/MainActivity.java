package com.softwind.softmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class
 *
 * @author jasmailduck
 */
public class MainActivity extends AppCompatActivity implements UIUpdateFromServiceHandle {

    public final int SONG_ORGINAL_TYPE = 0;


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

    //Will be optimized
    private boolean taskKill = true;


    //------------VIEWS & ADAPTERS-------------//

    //Holds the main screen fragment view
    private final HomeContent HOME_FRAG = new HomeContent();

    //Holds the imageview of the album art in the music player
    private RoundedImageView album;

    //Stores the seekbar view
    private SeekBar seekBar;

    //Runnable needed for updating the seekbar
    private Runnable runnable;

    //Handler to control the speed of the runnable using post delay
    private Handler handler;

    //Stores the next track button
    private ImageButton nextTrack;

    //Stores the play pause button
    private ImageButton playPause;

    //Stores the back button
    private ImageButton previousTrack;

    //Stores the song text
    private TextView songTextView;

    //Stores the artist text
    private TextView artistTextView;


    View view;

    private int lightSwatch;
    private int lightMuteSwatch;
    private int muteSwatch;

    private int darkuteSwatch;
    private int darkvibrantSwatch;
    private int dominantSwacth;

    private boolean stat = false;

    MusicPlayerService musicService;
    Boolean mIsBound;

    Intent serviceIntent;

    //--------------------SERVICES MANAGER--------------------//

    /**
     * Starts the services that initiated the media player related methods
     */
    private void startService() {
        serviceIntent = new Intent(this, MusicPlayerService.class);
        startForegroundService(serviceIntent);
        bindService();
    }

    /**
     * Stops services
     */
    private void stopService() {
        stopService(serviceIntent);
    }

    /**
     * Binds the service to the main activity.
     * Enables access to public methods inside of the service
     */
    private void bindService() {
        Intent serviceBindIntent = new Intent(this, MusicPlayerService.class);
        bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Gets the service from the binder and stores the instance for global access
     * Callbacks are set so the service can access interface methods from the main activity.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {

            // We've bound to MyService, cast the IBinder and get MyBinder instance
            MusicPlayerService.MyBinder binder = (MusicPlayerService.MyBinder) iBinder;
            musicService = binder.getService();
            mIsBound = true;
            musicService.setCallbacks(MainActivity.this);
            buttonListnerSetup();
            if(musicService.isPlaying()){
                setMediaPlayerInfo(musicService.getIndex());
                updateSeekBar();
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            mIsBound = false;
        }
    };

    //--------------------PRIVATE METHODS--------------------//

    /**
     * Simple method to control the fragment view
     *
     * @param fragment
     */
    private void fragmentManger(Fragment fragment) {
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
     * Extracts the metadata from the song, if there aren't any for some files, a default theme
     * is applied.
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

    /**
     * A custom string formatter. Formats the string to hardcoded requirments.
     * May need some tuning but not the biggest priority.
     *
     * @param songName
     * @return
     */
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

    /**
     * Changes aspects of a given drawable file
     * Currently is only set for changing the colour to the given swatch type colour
     *
     * @param drawableFile
     * @param swatchType
     * @return Drawable
     */
    private Drawable drawablecolorChanger(int drawableFile, int swatchType) {
        Drawable drawable = AppCompatResources.getDrawable(this, drawableFile);
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, swatchType);
        return wrappedDrawable;
    }

    /**
     * Sets up the button listeners
     * Prolly wont need this in futures, as I will be making a method
     * where the app can't start unless the service is active.
     */
    private void buttonListnerSetup() {

        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.next();

            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playPause();

            }
        });
        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.back();
            }
        });

    }


    /**
     * Converts a bitmap image to a {@link Uri} reference.
     * Loads the image to the respected Image View container
     *
     * @param pContext
     * @param pBitmap
     * @param pImageView
     */
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




    //------------------PUBLIC METHODS-----------------------//

    /**
     * Loads all the song information on tho the media widget
     *
     * @param position
     */
    public void setMediaPlayerInfo(int position) {
        Bitmap art = ListOfSongs.listOfSongs.get(position).getArt();

        loadBitmapByPicasso(this, art, album);

        songTextView.setText(ListOfSongs.listOfSongs.get(position).getSongName());
        songTextView.setTextColor(Palette.from(art).generate().getDarkMutedColor(Color.BLACK));
        artistTextView.setText(ListOfSongs.listOfSongs.get(position).getArtistName());
        artistTextView.setTextColor(Palette.from(art).generate().getDarkMutedColor(Color.BLACK));

        nextTrack.setBackground(drawablecolorChanger(R.drawable.ic_track_seek_next, Palette.from(art).generate().getDarkVibrantColor(Color.BLACK)));
        previousTrack.setBackground(drawablecolorChanger(R.drawable.ic_track_seek_back, Palette.from(art).generate().getDarkVibrantColor(Color.BLACK)));
        playPause.setBackground(drawablecolorChanger(R.drawable.ic_play, Palette.from(art).generate().getDarkVibrantColor(Color.BLACK)));
        view.setBackground(drawablecolorChanger(R.drawable.rounded_rec, Palette.from(art).generate().getLightMutedColor(Color.BLACK)));

        //seekBar.setProgressDrawable(AppCompatResources.getDrawable(this, R.drawable.seekbarfulllist));
        seekBar.setBackground(drawablecolorChanger(R.drawable.progressback, Palette.from(art).generate().getLightMutedColor(Color.BLACK)));
        seekBar.setThumb(drawablecolorChanger(R.drawable.ovalthumb,Palette.from(art).generate().getDarkVibrantColor(Color.BLACK)));
        seekBar.setProgressDrawable(drawablecolorChanger(R.drawable.seekbarfulllist,Palette.from(art).generate().getDarkMutedColor(Color.BLACK)));

    }

    /**
     * Methods is used by the fragments to play the song at a given touch position
     *
     * @param position
     */
    public void play(int position) {
        setMediaPlayerInfo(position);
        musicService.play(position);
    }

    /**
     * Responsible for syncing the music player with the seek bar
     */
    public void seekBarUpdater() {
        if (taskKill) {
            double c = ((double) musicService.getCurrentPosition() / musicService.getDuration() * 100);

            seekBar.setProgress((int) c, true);

            runnable = new Runnable() {
                @Override
                public void run() {

                    seekBarUpdater();
                }
            };
        }

        handler.postDelayed(runnable, 1000);

    }


    //------------------OVERRIDES---------------------//


    /**
     * Gets the current playing song from the service and updates the current music widget
     * accordingly
     */
    @Override
    public void updateUIInfo(int i) {
        setMediaPlayerInfo(i);
    }

    /**
     * Basically the method that gets called when an activity gets launched.
     * Responsible for initializing and starting other methods
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //-----------DEFAULT ANDROID STUFF--------//
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //-----------INITIALIZERS----------//
        handler = new Handler();
        seekBar = findViewById(R.id.music_seeker);
        nextTrack = findViewById(R.id.next_track_button);
        playPause = findViewById(R.id.play_button);
        previousTrack = findViewById(R.id.back_track_button);
        listOfSongPaths = findSongs(Environment.getExternalStorageDirectory().getAbsolutePath());
        album = findViewById(R.id.album_art_player);
        songTextView = findViewById(R.id.song_name_player);
        songTextView.setSelected(true);
        artistTextView = findViewById(R.id.artist_name_player);
        album.setImageResource(R.drawable.missing_art);
        view = findViewById(R.id.music_player_widget);
        seekBar.setBackground(AppCompatResources.getDrawable(this, R.drawable.rounded_rec));
        //-------------METHODS--------------//
        permissionHandler();
        metadataExtraction();
        startService();
        fragmentManger(HOME_FRAG);


        //-------------LISTENERS------------//

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            long f;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double l = progress / 100.0;
                double q = l * ((double) musicService.getDuration());
                f = (new Double(q)).longValue();
                if (fromUser) {
                    seekBar.setProgress(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                taskKill = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                taskKill= true;
                musicService.seekTo(f);
                seekBarUpdater();
            }
        });


    }

    /**
     * Updates and starts the seekbar update runnable
     */
    @Override
    public void updateSeekBar() {
        seekBar.setMax(100);
        seekBarUpdater();
    }

    @Override
    protected void onDestroy() {
        //stopService(); //Only enable for debugging
        super.onDestroy();
    }

    
}
