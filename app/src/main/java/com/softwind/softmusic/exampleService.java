package com.softwind.softmusic;

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
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.io.File;
import java.io.OutputStream;

public class exampleService extends Service {

    //Stores the metadata retriever.
    private MediaMetadataRetriever metadataRetriever;

    private ServiceInter serviceInter;

    //Stores the musicplayer
    private SimpleExoPlayer songPlayer;


    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;

    private PlayerNotificationManager playerNotificationManager;

    private MediaItem mediaItem;

    private final IBinder mBinder = new MyBinder();



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent pI = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

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

                .build();

        songPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                setMediaPlayerInfo(songPlayer.getCurrentWindowIndex());
                serviceInter.updateSeekBar();
            }
        });
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        playerNotificationManager.setPlayer(songPlayer);




       return START_STICKY;
    }
    public int getIndex(){
        return songPlayer.getCurrentWindowIndex();
    }


    public void setCallbacks(ServiceInter callbacks) {
        serviceInter = callbacks;
    }





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
        serviceInter.updateSeekBar();
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

    public void play(int position) {
        setMediaPlayerInfo(position);
        songPlayer.seekToDefaultPosition(position);
        playPause();
    }

    public void seekTo(long l){
        songPlayer.seekTo(l);
    }

    public long getDuration(){
        return songPlayer.getDuration();
    }

    public void setMediaPlayerInfo(int i){
        if (serviceInter != null) {
            serviceInter.doSomething(i);
        }
    }

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

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter{

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return player.getMediaItemAt(player.getCurrentWindowIndex()).mediaMetadata.title;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {

            return null;
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
    public class MyBinder extends Binder {
        exampleService getService() {
            // Return this instance of MyService so clients can call public methods
            return exampleService.this;
        }
    }
}
