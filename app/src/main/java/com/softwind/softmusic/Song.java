package com.softwind.softmusic;

import android.graphics.Bitmap;

import java.io.File;
import java.util.Objects;

public class Song {
    private String songName;
    private String artistName;
    private String albumName;
    private String genreName;
    private File path;
    private Bitmap art;



    public Song(String song, String artist, String album, String genre, File path, Bitmap art){
        songName=song;
        artistName = artist;
        albumName = album;
        genreName = genre;
        this.path = path;
        this.art = art;

    }


    public Bitmap getArt() {
        return art;
    }

    public void setArt(Bitmap art) {
        this.art = art;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return songName.equals(song.songName) &&
                path.equals(song.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songName, path);
    }
}

