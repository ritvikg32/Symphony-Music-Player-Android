package com.example.youtuberebuild;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class InsidePlaylistFragment extends Fragment {

    private TextView playlistName,miniTitle,miniArtist;
    private ImageView miniAlbumArt;
    private String plName;
    private ListView insideListView;
    private ArrayList<Song> songList = new ArrayList<Song>();
    private PlaylistChildDatabaseHelper plDB;
    private int PID;
    private MusicService musicSrv;
    private boolean musicBound = false;
    private Intent intent,playIntent;
    private LinearLayout miniLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.songlist_fragment,
                new PlaylistFragment()).commit();
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            //get service
            musicSrv = binder.getSerivce();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.inside_playlist_menu,menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(getContext(),MusicService.class);
            getContext().bindService(playIntent,musicConnection, Context.BIND_AUTO_CREATE);
            playIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            //  getContext().startService(playIntent);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int listPosition = info.position;
        Cursor data = plDB.getItemID(PID,songList.get(listPosition).getTitle());
        int ItemId = -1;

        while(data.moveToNext()){
            ItemId = data.getInt(0);
        }
        switch (item.getItemId()){

            case R.id.pl_song_delete:
                try{
                    plDB.deleteName(ItemId,songList.get(listPosition).getTitle());refreshFragment();
                    Toast.makeText(getContext(),"Deleted from playlist", LENGTH_LONG).show();
                }
                catch (Exception e){
                    Toast.makeText(getContext(),"Something went wrong! Try again later", LENGTH_LONG).show();
                }

            break;


        }
        return super.onContextItemSelected(item);
    }
    private void refreshFragment(){
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inside_playlist,container,false);
        playlistName = (TextView) view.findViewById(R.id.inside_playlist_text);
        insideListView = (ListView) view.findViewById(R.id.inside_playlist_listview);

        Bundle bundle = getArguments();
        plName = bundle.getString("playlistName");
        playlistName.setText(plName);
        PID = bundle.getInt("PID");

        plDB = new PlaylistChildDatabaseHelper(getContext());
        Cursor data = plDB.getListContents();
        if(data.getCount()==0){
            Toast.makeText(getContext(),"Oops! Your playlist is empty",Toast.LENGTH_LONG).show();
        }
        else{
            while(data.moveToNext()) {

                if (data.getInt(2) == PID) {
                    byte[] songAsBytes = data.getBlob(1);
                    try {
                        Song song = (Song) convertFromBytes(songAsBytes);
                        songList.add(song);
                    } catch (IOException e) {

                    } catch (ClassNotFoundException t) {

                    }


                }
            }
        }

        SongAdapter SongAdt = new SongAdapter(getActivity(),songList);
        insideListView.setAdapter(SongAdt);
        registerForContextMenu(insideListView);
        insideListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                intent = new Intent(getActivity(),SongPlayer.class);
                intent.putExtra("CURR_SONG_INDEX",i);
                intent.putExtra("SONG_LIST",songList);
                SongAdapter.mSelectedItem=songList.get(i).getTitle();
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                playIntent = new Intent(getActivity(),MusicService.class);
                getContext().startService(playIntent);
                musicSrv.setSong(i);
                setMiniLayout(i,container);
                musicSrv.playSong();
                startActivity(intent);
                //draggable.setVisibility(View.VISIBLE);

            }
        });





        return view;
    }

    private void setMiniLayout(int i,ViewGroup container){
        View view = getLayoutInflater().inflate(R.layout.fragment_all_songs,container,false);
        miniLayout = view.findViewById(R.id.mini_layout);
        miniTitle=miniLayout.findViewById(R.id.mini_title);
        miniArtist=miniLayout.findViewById(R.id.mini_artist);
        miniAlbumArt = (ImageView)miniLayout.findViewById(R.id.mini_album_art);
        try {
            Bitmap bm = Song.getAlbumArt(getContext(),songList.get(i).getId());
            miniAlbumArt.setImageBitmap(bm);
            miniTitle.setText(songList.get(musicSrv.getIndex()).getTitle());
            miniArtist.setText(songList.get(musicSrv.getIndex()).getArtist());
        }
        catch (Exception e){

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }



}
