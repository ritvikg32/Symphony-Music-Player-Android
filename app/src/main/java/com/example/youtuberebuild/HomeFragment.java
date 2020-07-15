package com.example.youtuberebuild;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static android.widget.Toast.LENGTH_LONG;

public class HomeFragment extends Fragment {
    private boolean firstClicked = false;
    private boolean musicBound;
    private LinearLayout draggable;
    private TextView miniTitle;
    private TextView miniArtist;
    private ImageView miniAlbumArt;
    public Intent intent;
    MusicService musicSrv;
    private DatabaseHelper myDB;
    private ArrayList<String> parent_playlist;
    private PlaylistChildDatabaseHelper plDB;
    private boolean FLAG = true;
    public int mSelectedItem=-1;
    private Intent playIntent;
    private LinearLayout miniLayout;
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            //get service
            musicSrv = binder.getSerivce();
            //pass list
            musicSrv.setList(songList);
            //setsong

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    ListView songView;
    SongAdapter SongAdt;
    private ArrayList<Song> songList = new ArrayList<Song>();

private int listPosition;
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        listPosition = info.position;

        switch (item.getItemId()){

            case R.id.select_song_option: selectPlaylistDialog(); break;

        }
        return super.onContextItemSelected(item);
    }

    public void selectPlaylistDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.select_playlist,null);

        ListView playlistSelect = (ListView) view.findViewById(R.id.playlist_select_listview);
        myDB = new DatabaseHelper(getContext());
        parent_playlist = new ArrayList<String>();
        Cursor data = myDB.getListContents();

        if(data.getCount()==0){
            Toast.makeText(getContext(),"No Playlist Created Yet", LENGTH_LONG).show();
        }
        else{
            while(data.moveToNext()){
                parent_playlist.add(0,data.getString(1));
                ListAdapter listAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,parent_playlist);
                playlistSelect.setAdapter(listAdapter);
            }
        }




        //Building the dialog layout
        builder.setView(view)
                .setTitle("Select Playlist")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Nothing here
                    }
                });


        builder.show();

        playlistSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                byte[] songAsBytes = convertToByte(songList.get(listPosition));
                Cursor data = myDB.getItemID(parent_playlist.get(i));
                int ItemId = -1;

                while(data.moveToNext()){
                    ItemId = data.getInt(0);
                }
                plDB = new PlaylistChildDatabaseHelper(getContext());
                AddData(songAsBytes,ItemId,songList.get(listPosition).getTitle());
                //plDB.addData(songAsBytes,ItemId);


                //Toast.makeText(getContext(),"Added Successfully", LENGTH_LONG).show();
            }
        });

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


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.longpress_home_list,menu);
    }


    @Override
    public void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(getContext(),MusicService.class);
            getContext().bindService(playIntent,musicConnection,Context.BIND_AUTO_CREATE);
            playIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
          //  getContext().startService(playIntent);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_songs,container,false);

        songView= (ListView) view.findViewById(R.id.song_list);
        registerForContextMenu(songView);
        miniLayout=(LinearLayout)view.findViewById(R.id.mini_layout);
        intent = new Intent(getActivity(),SongPlayer.class);

        SongAdt = new SongAdapter(getContext(),songList);
        songView.setAdapter(SongAdt);
        SongAdt.notifyDataSetChanged();
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                firstClicked=true;
                FLAG=true;
                SongAdapter.mSelectedItem=songList.get(i).getTitle();
                SongAdt.notifyDataSetChanged();
                songView.setAdapter(SongAdt);

                intent.putExtra("SONG_LIST",songList);

                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                playIntent = new Intent(getActivity(),MusicService.class);
               // getActivity().bindService(playIntent,musicConnection,Context.BIND_AUTO_CREATE);
                getActivity().startService(playIntent);
                musicSrv.setSong(i);
                setMiniLayout(i);

                musicSrv.playSong();
                startActivity(intent);


            }
        });
        miniLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                musicSrv.getMediaPLayer().start();
                intent.putExtra("SONG_LIST",songList);
                startActivity(intent);
            }
        });


        checkUserPermission();
        if(musicSrv!=null)
            setMiniLayout(musicSrv.SongPosn);
        return view;
    }
    private void checkUserPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);
                return;
            }
        }
        loadSongs();
    }

    private void setMiniLayout(int i){
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

    @Override
    public void onResume() {
        super.onResume();
        if(musicSrv!=null)
            setMiniLayout(musicSrv.SongPosn);
        SongAdt.notifyDataSetChanged();
        songView.setAdapter(SongAdt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadSongs();
                }else{
                    Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    checkUserPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }


    public void loadSongs(){
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


        Cursor musicCursor = musicResolver.query(musicUri,null,null,null,null);



        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get column numbers of the data stored
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);

            //retrieve data stored in their respective column numbers

            do{
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                //String albumARt = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                songList.add(new Song(thisId,thisTitle,thisArtist));
            }
            while(musicCursor.moveToNext());

            musicCursor.close();
            SongAdt = new SongAdapter(getContext(),songList);
        }
    }
    public void AddData(byte [] bytes, int plId, String name){
        boolean insertData = plDB.addData(bytes,plId,name);

        if(insertData){
            Toast.makeText(getContext(),"Song Added!", LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getContext(), "Something went wrong :(.", Toast.LENGTH_LONG).show();
        }
        //Refreshes the current fragment
       // getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

}
