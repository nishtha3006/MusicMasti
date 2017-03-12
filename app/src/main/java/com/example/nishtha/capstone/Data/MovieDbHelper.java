package com.example.nishtha.capstone.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "songLyrics.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to store fav songs.
        final String SQL_CREATE_SONG_TABLE = "CREATE TABLE " + MovieContract.Favourite.TABLE_NAME + " (" +
                MovieContract.Favourite._ID + " INTEGER, " +
                MovieContract.Favourite.COLUMN_TITLE + " TEXT UNIQUE NOT NULL, " +
                MovieContract.Favourite.COLUMN_ARTIST + " TEXT NOT NULL, " +
                MovieContract.Favourite.COLUMN_IMAGE_URL + " TEXT, " +
                "PRIMARY KEY (" + MovieContract.Favourite.COLUMN_TITLE + ", " + MovieContract.Favourite.COLUMN_TITLE + ") " +
                " );";

        // Create table to store search results and most popular
        final String SQL_CREATE_SEARCH_TABLE = "CREATE TABLE " + MovieContract.Song.TABLE_NAME + " (" +
                MovieContract.Song._ID + " INTEGER, " +
                MovieContract.Song.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieContract.Song.COLUMN_ARTIST + " TEXT NOT NULL, " +
                MovieContract.Song.COLUMN_IMAGE_URL + " TEXT, " +
                "PRIMARY KEY (" + MovieContract.Song.COLUMN_TITLE + ", " + MovieContract.Song.COLUMN_TITLE + ") " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_SONG_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SEARCH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.Favourite.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.Song.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
