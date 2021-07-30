package com.softwind.softmusic;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

public class MediaHandler {
    private static MediaPlayer mediaPlayer;

    public static void playMedia(Context context, int position){
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
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    public static void pauseMedia(){
        mediaPlayer.pause();
    }

    public static void stopMedia(){
        mediaPlayer.release();
    }
}
