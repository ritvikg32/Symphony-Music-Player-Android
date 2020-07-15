package com.example.youtuberebuild;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class PlaylistFragment extends Fragment implements PlayListEnterDialog.PlaylistDialogListener {
    private ArrayList<String> playListItem;
    private ImageButton addPlaylistButton;
    private PlayListAdapter playAdt;
    private GridView playView;
    private EditText playlistNameEntered;
    private String setPlaylistTitle;
    private DatabaseHelper myDB;
    private ArrayList<String> theList;
    private boolean isFirstRun;
    private ImageButton addPlButton;
    public ArrayList<PlayList> pl = new ArrayList<PlayList>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.longclick_playlist_menu,menu);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int listPosition = info.position;
        Cursor data = myDB.getItemID(theList.get(listPosition));
        int ItemId = -1;

        while(data.moveToNext()){
            ItemId = data.getInt(0);
        }

        switch (item.getItemId()){

            case R.id.delete_option:
                     myDB.deleteName(ItemId,theList.get(listPosition));refreshFragment();
                Toast.makeText(getContext(),"Deleted Successfully", LENGTH_LONG).show();
                                        break;
            case R.id.add_songs_option: Toast.makeText(getContext(),"Songs Added", LENGTH_LONG).show();
                break;

            case R.id.rename_option: openRenameDialog(ItemId,theList.get(listPosition)); refreshFragment();break;
        }


        return super.onContextItemSelected(item);
    }
    private void refreshFragment(){
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }
    public void openRenameDialog(final int itemId, final String oldName){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.rename_dialog,null);

        //Building the dialog layout
        builder.setView(view)
                .setTitle("Rename Playlist")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Nothing here
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nameInput = playlistNameEntered.getText().toString();
                        if(nameInput!=null || nameInput.length()!=0){
                            myDB.updateName(nameInput,itemId,oldName);
                        }
                        else{
                            Toast.makeText(getContext(),"Please Enter something!", LENGTH_LONG).show();
                        }


                    }
                });
        playlistNameEntered =  view.findViewById(R.id.new_name_enter);
        builder.show();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {

        final View playlistFragmentView = inflater.inflate(R.layout.fragment_playlist,container,false);
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);

        myDB = new DatabaseHelper(getContext());
        playView = (GridView) playlistFragmentView.findViewById(R.id.playListId);
        theList = new ArrayList<String>();
        Cursor data = myDB.getListContents();
        registerForContextMenu(playView);
        addPlButton = playlistFragmentView.findViewById(R.id.add_button);
        addPlButton = playlistFragmentView.findViewById(R.id.add_button);
        if(data.getCount()==0){
            Toast.makeText(getContext(),"No Playlist Created Yet", LENGTH_LONG).show();
        }
        else{
            while(data.moveToNext()){

                    theList.add(data.getString(1));
                    ListAdapter listAdapter = new ArrayAdapter<>(getContext(), R.layout.playlist, theList);
                    playView.setAdapter(listAdapter);

            }
        }
        playView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Bundle bundle = new Bundle();
                bundle.putString("playlistName",theList.get(i));

                Cursor data = myDB.getItemID(theList.get(i));
                int ItemId = -1;

                while(data.moveToNext()){
                    ItemId = data.getInt(0);
                }
                bundle.putInt("PID",ItemId);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                InsidePlaylistFragment insidePlaylistFragment = new InsidePlaylistFragment();
                insidePlaylistFragment.setArguments(bundle);



                fragmentTransaction.replace(R.id.songlist_fragment,
                        insidePlaylistFragment).commit();
            }
        });

        //loadData();




        addPlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        return playlistFragmentView;
    }

    public void openDialog(){
        PlayListEnterDialog dialog = new PlayListEnterDialog();
        dialog.setTargetFragment(PlaylistFragment.this,1);
        dialog.show(getFragmentManager(),"create playlist");

    }


    @Override
    public void applyTexts(String name) {
        if(name==null || name.length()==0){
            Toast.makeText(getContext(),"Please Enter something", LENGTH_LONG).show();
        }


        else{
            AddData(name);
        }
    }
    public void AddData(String name){
        boolean insertData = myDB.addData(name);

        if(insertData){
            Toast.makeText(getContext(),"Playlist Created Successfully", LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getContext(), "Something went wrong :(.", Toast.LENGTH_LONG).show();
        }
        //Refreshes the current fragment
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }


}
