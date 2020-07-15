package com.example.youtuberebuild;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayListAdapter extends BaseAdapter {
    ArrayList<Song> songs;
    LayoutInflater songInf;
    String artist_name;

    public PlayListAdapter(HomeFragment c, ArrayList<Song> songs) {
        this.songs = songs;
        songInf = LayoutInflater.from(c.getContext());
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout)songInf.inflate(R.layout.song,parent,false);//Doubt//
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);


        Song currSong = songs.get(position);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        songLay.setTag(position);
        return songLay;
    }
}
