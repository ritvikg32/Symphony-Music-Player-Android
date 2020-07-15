package com.example.youtuberebuild;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class PlayListEnterDialog extends DialogFragment {

    private EditText playlistNameEntered;
    private PlaylistDialogListener listener;



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (PlaylistDialogListener) getTargetFragment();
        }
        catch (ClassCastException e){

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog,null);

        //Building the dialog layout
        builder.setView(view)
                .setTitle("Create Playlist")
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
                        listener.applyTexts(nameInput);
                    }
                });
        playlistNameEntered =  view.findViewById(R.id.pl_title_enter);

        return builder.create();
    }




    public interface PlaylistDialogListener{
         void applyTexts(String name);
    }

}
