package com.example.youtuberebuild;

public class PlayList {

    private String plTitle;
    private int noSongs;

    public PlayList(String plTitle) {
        this.plTitle = plTitle;
    }

    public void setPlTitle(String plTitle) {
        this.plTitle = plTitle;
    }

    public String getPlTitle() {
        return plTitle;
    }

    public int getNoSongs() {
        return noSongs;
    }
}
