package com.example.youtuberebuild;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Base64;

import java.io.FileDescriptor;
import java.io.Serializable;

public class Song implements Serializable {

    private long id;
    private String title;
    private  String artist;
    private String albArt;

    public Song(long id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;

    }


    public static Bitmap getAlbumArt(Context context, long songId){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songId);
        mediaMetadataRetriever.setDataSource(context,trackUri);
        try{
            byte[] art = mediaMetadataRetriever.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory
                    .decodeByteArray(art, 0, art.length);
            return songImage;
        }catch (Exception e){
            //Set Default album art
        }
        return null;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }



}
