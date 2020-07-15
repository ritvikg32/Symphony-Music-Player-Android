package com.example.youtuberebuild;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class SongAdapter extends BaseAdapter {
    ArrayList<Song> songs;
    LayoutInflater songInf;
    String artist_name;
    public static String mSelectedItem="n";
    private Context context;
    private EqualizerView equalizer;
    private ImageView listAlbumA;

    public SongAdapter(Context c, ArrayList<Song> songs) {
        this.songs = songs;
        songInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position)
    {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout)songInf.inflate(R.layout.song,parent,false);//Doubt//
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);










        Song currSong = songs.get(position);
        if(mSelectedItem.equals(currSong.getTitle())){
            songView.setTextColor(context.getColor(R.color.themeColor));
            artistView.setTextColor(context.getColor(R.color.themeColor));

        }
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        songLay.setTag(position);
        return songLay;
    }
}
