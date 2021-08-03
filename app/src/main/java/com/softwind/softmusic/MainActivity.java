package com.softwind.softmusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.MetadataRetriever;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.common.util.concurrent.ListenableFuture;
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
public class MainActivity extends AppCompatActivity {

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

    //Stores the musicplayer
    private SimpleExoPlayer songPlayer;

    //Will be optimized
    private boolean taskKill = true;

    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;


    //------------VIEWS & ADAPTERS-------------//

    //Holds the main screen fragment view
    private final HomeContent HOME_FRAG = new HomeContent();

    //Holds the imageview of the album art in the music player
    private RoundedImageView album;

    //Stores the seekbar view
    private SeekBar seekBar;

    //Runnable needed for updating the seekbar
    private Runnable runnable;

    //E
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

    //Converts and stores the Song object into a Media Item object
    private MediaItem mediaItem;

    View view;

    private int lightSwatch;
    private int lightMuteSwatch;
    private int muteSwatch;

    private int darkuteSwatch;
    private int darkvibrantSwatch;
    private int dominantSwacth;

    private PlayerNotificationManager playerNotificationManager;


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
        fragmentManger(HOME_FRAG);
        mediaManager(SONG_ORGINAL_TYPE);


        //-------------LISTENERS------------//
        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();

            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();

            }
        });
        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double l = progress / 100.0;
                double q = l * ((double) songPlayer.getDuration());
                long f = (new Double(q)).longValue();
                if (fromUser) {

                    songPlayer.seekTo(f);

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
        songPlayer.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {

            }
        });

        songPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {

                setMediaPlayerInfo(songPlayer.getCurrentWindowIndex());
                seekBar.setMax(100);
                seekBarUpdater();
            }
        });
        //-------TEMP OR TESTING-----------//

        palatteGen(ListOfSongs.listOfSongs.get(songPlayer.getCurrentWindowIndex()).getArt());




    }

    private void playPause() {
        if (songPlayer.isPlaying()) {
            songPlayer.pause();
        } else {
            songPlayer.play();

        }
    }

    /**
     * Advances the song to the next
     *
     * @param
     */
    private void next() {
        songPlayer.next();
        setMediaPlayerInfo(songPlayer.getCurrentWindowIndex());
    }

    /**
     * Goes back o the previous song
     *
     * @param
     */
    private void back() {
        songPlayer.previous();
        setMediaPlayerInfo(songPlayer.getCurrentWindowIndex());
    }

    private Uri bitmaptoUri(Context pContext, Bitmap pBitmap){
        try {
            Uri uri = Uri.fromFile(File.createTempFile("temp_file_name", ".jpg", pContext.getCacheDir()));
            OutputStream outputStream = pContext.getContentResolver().openOutputStream(uri);
            pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            return uri;
        } catch (Exception e) {

        }
        return null;
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

    Palette palette;
    public void palatteGen(Bitmap bitmap){



        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                lightSwatch = palette.getLightMutedColor(Color.RED);
                lightMuteSwatch = palette.getLightMutedColor(Color.BLACK);
                muteSwatch = palette.getMutedColor(Color.BLACK);

                darkuteSwatch = palette.getDarkMutedColor(Color.BLACK);
                darkvibrantSwatch = palette.getDarkVibrantColor(Color.BLACK);
                dominantSwacth = palette.getDominantColor(Color.BLACK);

            }
        });


    }

    //------------------PUBLIC METHODS-----------------------//

    /**
     * Loads all the song information on tho the media widget
     *
     * @param position
     */
    public void setMediaPlayerInfo(int position) {
        Bitmap art = ListOfSongs.listOfSongs.get(position).getArt();
        palatteGen(art);
        loadBitmapByPicasso(this, art, album);

        songTextView.setText(ListOfSongs.listOfSongs.get(position).getSongName());
        songTextView.setTextColor(darkuteSwatch);
        artistTextView.setText(ListOfSongs.listOfSongs.get(position).getArtistName());
        artistTextView.setTextColor(darkuteSwatch);

        nextTrack.setBackground(drawablecolorChanger(R.drawable.ic_track_seek_next, darkuteSwatch));
        previousTrack.setBackground(drawablecolorChanger(R.drawable.ic_track_seek_back,darkuteSwatch));
        playPause.setBackground(drawablecolorChanger(R.drawable.ic_play,darkuteSwatch));
        view.setBackground(drawablecolorChanger(R.drawable.rounded_rec, muteSwatch));
        seekBar.setBackground(drawablecolorChanger(R.drawable.rounded_rec, darkuteSwatch));



    }



    private Drawable drawablecolorChanger(int drawableFile, int swatchType){
        Drawable drawable = AppCompatResources.getDrawable(this,drawableFile);
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, swatchType);
        return wrappedDrawable;
    }

    /**
     * Responsible for playing the song
     *
     * @param
     */
    public void mediaManager(int playlistType) {

        if (playlistType == 0) {
            songPlayer = new SimpleExoPlayer.Builder(MainActivity.this).build();
            for (int i = 0; i < ListOfSongs.listOfSongs.size(); i++) {

                MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                        .setTitle(ListOfSongs.listOfSongs.get(i).getSongName())
                        .setAlbumArtist(ListOfSongs.listOfSongs.get(i).getArtistName())
                        .setAlbumTitle(ListOfSongs.listOfSongs.get(i).getAlbumName())
                        .setArtworkUri(bitmaptoUri(this,ListOfSongs.listOfSongs.get(i).getArt()))
                        .build();

                mediaItem = new MediaItem.Builder()
                        .setUri(Uri.fromFile(ListOfSongs.listOfSongs.get(i).getPath()))
                        .setMediaMetadata(mediaMetadata)
                        .build();

                songPlayer.addMediaItem(mediaItem);

            }
            songPlayer.prepare();
        }

        mediaSession = new MediaSessionCompat(this, "media");
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(songPlayer);
        mediaSession.setActive(true);

        MediaSessionConnector.QueueNavigator queueNavigator = new TimelineQueueNavigator(mediaSession) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                MediaDescriptionCompat mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                        .setMediaUri(player.getMediaItemAt(windowIndex).mediaMetadata.mediaUri)
                        .setTitle(player.getMediaItemAt(windowIndex).mediaMetadata.title)
                        .build();
                return mediaDescriptionCompat;
            }
        };

        mediaSessionConnector.setQueueNavigator(queueNavigator);
        NotificationChannel notificationChannel = new NotificationChannel("Music", "Music", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);

        playerNotificationManager = new PlayerNotificationManager.Builder(this,544,"Music" ,new DescriptionAdapter())
                .build();

        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        playerNotificationManager.setPlayer(songPlayer);

    }



    public void play(int position) {
        setMediaPlayerInfo(position);
        songPlayer.seekToDefaultPosition(position);
        playPause();
    }

    /**
     * Responsible for syncing the music player with the seek bar
     */
    public void seekBarUpdater() {
        if (taskKill) {
            double c = ((double) songPlayer.getCurrentPosition() / songPlayer.getDuration() * 100);

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

    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter{

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return player.getMediaItemAt(player.getCurrentWindowIndex()).mediaMetadata.title;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {


            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addNextIntentWithParentStack(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);
            return resultPendingIntent;
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return player.getMediaItemAt(player.getCurrentWindowIndex()).mediaMetadata.albumArtist;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            try {
                return MediaStore.Images.Media.getBitmap(getContentResolver(),player.getMediaItemAt(player.getCurrentWindowIndex()).mediaMetadata.artworkUri);
            } catch (Exception e) {
                return null;
            }


        }
    }


}
