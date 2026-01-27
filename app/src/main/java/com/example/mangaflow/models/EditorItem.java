package com.example.mangaflow.models;

public class EditorItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_MANGA = 1;

    public int type;
    public String title;

    public EditorItem(int type, String title) {
        this.type = type;
        this.title = title;
    }
}