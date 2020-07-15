package com.example.youtuberebuild;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import com.example.youtuberebuild.MusicService.MyBinder;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.MediaController.MediaPlayerControl;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.widget.Toast.LENGTH_LONG;

public class SongPlayer extends AppCompatActivity implements View.OnClickListener{

    ImageButton exitB,prevB,nextB,playB;
    private static MusicService musicSrv;
    Drawable pause_button_drawable,play_button_drawable,default_album_art;
    private boolean FLAG = true;
    private MediaMetadataRetriever metadataRetriever;


    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this,MusicService.class);
            bindService(playIntent,musicConnection,Context.BIND_AUTO_CREATE);
            //startService(playIntent);
        }
    }

    private Intent playIntent;
    private boolean musicBound=false;



    TextView songT,artistN,time_rem,time_pl;
    ImageView albumA;
    SeekBar songPr;
    MediaPlayer player;
    ConstraintLayout song_player;
    private ArrayList<Song> songList;
    private Song currSong;
    private int currIndex,song_duration,song_played,curr_pos,oTime=0;
    private long minutes=0,seconds=0;
    private Handler hdlr = new Handler();
    public long albumId;
    private ImageButton backArrow,favouritesAddB;
    private boolean likeClicked = false;



    private ArrayList<Drawable> bg_collection_list = new ArrayList<Drawable>();

    @Override
    protected void onResume() {
        super.onResume();
       // playB.setBackground(play_button_drawable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_player);

        //player.reset();
        //Initializing UI elements;
        song_player = (ConstraintLayout) findViewById(R.id.player_screen);


        pause_button_drawable = getResources().getDrawable(R.drawable.ic_pause_circle_filled_24px);
        play_button_drawable = getResources().getDrawable(R.drawable.ic_play_circle_filled_24px);
        prevB = (ImageButton)findViewById(R.id.prev_button);
        playB = (ImageButton)findViewById(R.id.play_button);
        nextB = (ImageButton)findViewById(R.id.next_button);
        songT = (TextView)findViewById(R.id.song_title);
        artistN = (TextView)findViewById(R.id.artist_name);
        time_pl = (TextView)findViewById(R.id.time_played);
        time_rem = (TextView)findViewById(R.id.time_remaining);
        songPr = (SeekBar)findViewById(R.id.song_progress);
        albumA = (ImageView)findViewById(R.id.album_art);
        backArrow=(ImageButton)findViewById(R.id.back_down_arrow);
        favouritesAddB = (ImageButton)findViewById(R.id.like_button);
        final Drawable favouriteSelect = getResources().getDrawable(R.drawable.ic_thumb_up_select_24px);
        final Drawable favouriteDeSelect = getResources().getDrawable(R.drawable.ic_thumb_up_24px);
        //Exit button for the layout
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        favouritesAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(likeClicked){
                    favouritesAddB.setBackground(favouriteDeSelect);
                    likeClicked=false;
                }

                else {
                    likeClicked=true;
                    favouritesAddB.setBackground(favouriteSelect);
                    byte[] songAsBytes = convertToByte(songList.get(musicSrv.SongPosn));
                    DatabaseHelper myDB = new DatabaseHelper(getApplicationContext());


                        Cursor data = myDB.getItemID("Favourites");
                        int ItemId = -1;

                        while (data.moveToNext()) {
                            ItemId = data.getInt(0);
                        }
                        AddSongToPlaylist(songAsBytes, ItemId, songList.get(musicSrv.SongPosn).getTitle());



                }
            }
        });

        songPr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

                int x = (int) Math.ceil(progress / 1000f);



                double percent = progress / (double) seekBar.getMax();
                int offset = seekBar.getThumbOffset();
                int seekWidth = seekBar.getWidth();
                int val = (int) Math.round(percent * (seekWidth - 2 * offset));



            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                if (musicSrv.getMediaPLayer() != null && musicSrv.isPng()) {
                    musicSrv.seek(seekBar.getProgress());
                }
            }
        });

        Intent intent = getIntent();
        //currIndex = (Integer) intent.getSerializableExtra("CURR_SONG_INDEX");
        songList = (ArrayList<Song>) intent.getSerializableExtra("SONG_LIST");
        FLAG = intent.getBooleanExtra("FLAG",true);

        playB.setOnClickListener(this);
        nextB.setOnClickListener(this);
        prevB.setOnClickListener(this);
    }


    private void AddSongToPlaylist(byte[] bytes, int plId, String name){
        PlaylistChildDatabaseHelper plDB = new PlaylistChildDatabaseHelper(getApplicationContext());
        boolean insertData = plDB.addData(bytes,plId,name);

        if(insertData){
            Toast.makeText(getApplicationContext(),"Song Added!", LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Something went wrong :(.", Toast.LENGTH_LONG).show();
        }
    }
    public byte[] convertToByte(Song obj){

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            byte[] songAsBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(songAsBytes);
            return songAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            //get service
            musicSrv = binder.getSerivce();
            //pass list
            //musicSrv.setList(songList);
            //musicSrv.setSong(currIndex);
            musicSrv.FLAG = FLAG;
           // musicSrv.playSong();
            playSong();


            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    int playButtonClicked = 1;
    private boolean songChanged = true;

    @Override
    protected void onPause() {
        playButtonClicked=1;
        super.onPause();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.play_button:
                if(playButtonClicked==0){
                    playButtonClicked=1;

                    playB.setBackground(pause_button_drawable);
                    playSong();
                    musicSrv.go();





                }
                else if(playButtonClicked==1){
                    if(musicSrv.isPng()) {
                        musicSrv.pauseSong();
                        playButtonClicked = 0;
                        playB.setBackground(play_button_drawable);
                    }
                }
                break;
            case R.id.prev_button:
                musicSrv.FLAG=true;
                musicSrv.playPrev();
                playB.setBackground(pause_button_drawable);
                SongAdapter.mSelectedItem=songList.get(musicSrv.SongPosn).getTitle();
                playSong();
                break;
            case R.id.next_button:
                musicSrv.FLAG=true;
                musicSrv.playNext();
                playB.setBackground(pause_button_drawable);
                SongAdapter.mSelectedItem=songList.get(musicSrv.SongPosn).getTitle();
                playSong();
                break;
        }
    }




    //Update all the components of the layout based on the current song
    private void playSong(){
        int song_index = musicSrv.SongPosn;
        currSong=songList.get(song_index);
        artistN.setText(currSong.getArtist());
        try{
            albumA.setImageBitmap(Song.getAlbumArt(getApplicationContext(),currSong.getId()));
        }
        catch (Exception e){
            default_album_art = getResources().getDrawable(R.drawable.default_album_art);
            albumA.setBackground(default_album_art);
        }

        songT.setText(currSong.getTitle());
        song_duration=musicSrv.getDur();
        curr_pos=musicSrv.getPosn();
        seconds = TimeUnit.MILLISECONDS.toSeconds(curr_pos);
        minutes = TimeUnit.MILLISECONDS.toMinutes(curr_pos);
        long seconds_duration = TimeUnit.MILLISECONDS.toSeconds(song_duration);
        seconds_duration/=10;

        time_rem.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(song_duration),
                seconds_duration));
        hdlr.postDelayed(UpdateSongTime, 100);
        if(oTime==0){
            songPr.setMax(song_duration);
            oTime=1;
        }

        songPr.setProgress(curr_pos);

        //Bitmap bm = BitmapFactory.decodeFile(currSong.getAlbArt());
        //albumA.setImageBitmap(bm);
    }


    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            curr_pos = musicSrv.getPosn();
            seconds = TimeUnit.MILLISECONDS.toSeconds(curr_pos);
            minutes = TimeUnit.MILLISECONDS.toMinutes(curr_pos);
            seconds %=60;

            if(seconds<10)
                time_pl.setText(String.format("%d:0%d", minutes, seconds));
            else
            time_pl.setText(String.format("%d:%d", minutes, seconds));



            songPr.setProgress(curr_pos);
            hdlr.postDelayed(this, 100);
        }
    };
}
