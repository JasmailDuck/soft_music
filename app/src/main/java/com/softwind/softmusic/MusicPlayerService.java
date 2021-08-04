package com.softwind.softmusic;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.io.File;
import java.io.OutputStream;

public class MusicPlayerService extends Service {

    //----------------DATA-------------------//

    //Stores the metadata retriever.
    private MediaMetadataRetriever metadataRetriever;

    //Interface
    private UIUpdateFromServiceHandle UIUpdateFromServiceHandle;

    //Stores the music player
    private SimpleExoPlayer songPlayer;

    //Stores the mediaSession
    private MediaSessionCompat mediaSession;

    //Stores the connector
    private MediaSessionConnector mediaSessionConnector;

    //Stores the notifaction
    private PlayerNotificationManager playerNotificationManager;

    //Stores the mediaItem
    private MediaItem mediaItem;

    //Stores the binder
    private final IBinder mBinder = new MyBinder();

    //----------------PRIVATE METHODS---------------------//

    /**
     * Converts a given bitmap to a Uri
     * @param pContext
     * @param pBitmap
     * @return Uri
     */
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

    //-------------PUBLIC METHODS--------------------------//

    /**
     * Gets the media player's current playing song index
     * @return
     */
    public int getIndex(){
        return songPlayer.getCurrentWindowIndex();
    }

    /**
     *Sets the callback to be able to talk to the main activity
     * @param callbacks
     */
    public void setCallbacks(UIUpdateFromServiceHandle callbacks) {
        UIUpdateFromServiceHandle = callbacks;
    }

    /**
     * Plays or pauses the player depending on situation
     */
    public void playPause() {
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
    public void next() {
        songPlayer.next();
        setMediaPlayerInfo(songPlayer.getCurrentWindowIndex());
        UIUpdateFromServiceHandle.updateSeekBar();
    }

    /**
     * Goes back o the previous song
     *
     * @param
     */
    public void back() {
        songPlayer.previous();
        setMediaPlayerInfo(songPlayer.getCurrentWindowIndex());
    }

    /**
     * Plays the song at the given index
     * @param position
     */
    public void play(int position) {
        setMediaPlayerInfo(position);
        songPlayer.seekToDefaultPosition(position);
        playPause();
    }

    /**
     * Seeks the song to given long(position) in the song
     * @param l
     */
    public void seekTo(long l){
        songPlayer.seekTo(l);
    }

    /**
     * Gets the whole duration of the song
     * @return
     */
    public long getDuration(){
        return songPlayer.getDuration();
    }

    /**
     * Calls on the inderface to update the UI in the main class
     * @param i
     */
    public void setMediaPlayerInfo(int i){
        if (UIUpdateFromServiceHandle != null) {
            UIUpdateFromServiceHandle.updateUIInfo(i);
        }
    }

    /**
     * Gets the current position of the song in the terms of progress
     * @return
     */
    public long getCurrentPosition(){
        return songPlayer.getCurrentPosition();
    }

    /**
     * Responsible for playing the song
     *
     * @param
     */
    public void mediaManager(int playlistType) {

        if (playlistType == 0) {
            songPlayer = new SimpleExoPlayer.Builder(this).build();
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


    }

    //--------------------OVERRIDES-----------------//

    /**
     * Binds the service
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Runner for methods when the service is started
     * @param intent
     * @param flags
     * @param startId
     * @return int
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Intent pI = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,pI,0);

        mediaManager(0);

        NotificationChannel notificationChannel = new NotificationChannel("Music", "Music", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);

        playerNotificationManager = new PlayerNotificationManager.Builder(this,544,"Music" ,new DescriptionAdapter())
                .setNotificationListener(new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {

                    }

                    @Override
                    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                    startForeground(notificationId, notification);
                    }
                })
                .setPlayActionIconResourceId(R.drawable.ic_play)
                .setNextActionIconResourceId(R.drawable.ic_track_seek_next)
                .setPreviousActionIconResourceId(R.drawable.ic_track_seek_back)
                .build();

        songPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                setMediaPlayerInfo(songPlayer.getCurrentWindowIndex());
                UIUpdateFromServiceHandle.updateSeekBar();
            }
        });
        songPlayer.setHandleAudioBecomingNoisy(true);
        songPlayer.setForegroundMode(true);

        songPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),true);
        playerNotificationManager.setUseNextActionInCompactView(true);
        playerNotificationManager.setUsePreviousActionInCompactView(true);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        playerNotificationManager.setPlayer(songPlayer);




       return START_STICKY;
    }

    /**
     * Stops service when method is called
     */
    @Override
    public void onDestroy() {
        songPlayer.release();
        mediaSession.setActive(false);
        mediaSession.release();
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    //----------------------CLASSES-----------------------//

    /**
     * Class needed for the notification
     */
    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter{

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return player.getMediaItemAt(player.getCurrentWindowIndex()).mediaMetadata.title;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,0);
            return pendingIntent;
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


    /**
     * Sets up the binder
     */
    public class MyBinder extends Binder {
        MusicPlayerService getService() {
            // Return this instance of MyService so clients can call public methods
            return MusicPlayerService.this;
        }
    }
}
