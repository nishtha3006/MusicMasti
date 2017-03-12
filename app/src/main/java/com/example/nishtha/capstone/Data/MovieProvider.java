package com.example.nishtha.capstone.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;


public class MovieProvider extends ContentProvider{
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mSongHelper;

    static final int SONG = 100;
    static final int SEARCH = 101;
    static final int SONG_AT = 102;


    private static final SQLiteQueryBuilder FAV_QUERY_BUILDER;
    private static final SQLiteQueryBuilder SEARCH_QUERY_BUILDER;

    static{
        FAV_QUERY_BUILDER = new SQLiteQueryBuilder();
        FAV_QUERY_BUILDER.setTables(MovieContract.Favourite.TABLE_NAME);


        SEARCH_QUERY_BUILDER = new SQLiteQueryBuilder();
        SEARCH_QUERY_BUILDER.setTables(MovieContract.Song.TABLE_NAME);
    }


    //title = ? AND artist = ?
    private static final String sTitleAndArtistSelection = MovieContract.Favourite.COLUMN_TITLE + " = ? AND " +
            MovieContract.Favourite.COLUMN_ARTIST + " = ? ";

    private Cursor getSongByArtistTitle(Uri uri, String[] projection, String sortOrder) {
        String title = MovieContract.Favourite.getTitleFromUri(uri);
        String artist = MovieContract.Favourite.getArtistFromUri(uri);

        String selection = sTitleAndArtistSelection;
        String[] selectionArgs = new String[]{title, artist};

        return FAV_QUERY_BUILDER.query(mSongHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String contentAuthority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(contentAuthority, MovieContract.PATH_FAV, SONG);
        matcher.addURI(contentAuthority, MovieContract.PATH_FAV + "/*/*", SONG_AT);
        matcher.addURI(contentAuthority, MovieContract.PATH_SEARCH, SEARCH);

        return matcher;
    }

    public MovieProvider() {
    }

    @Override
    public boolean onCreate() {
        mSongHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mSongHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int Deleted;

        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";

        switch (match) {
            case SONG:
                Deleted = db.delete(MovieContract.Favourite.TABLE_NAME, selection, selectionArgs);
                break;
            case SEARCH:
                Deleted = db.delete(MovieContract.Song.TABLE_NAME, selection, selectionArgs);
                break;
            case SONG_AT:
                Deleted = db.delete(MovieContract.Favourite.TABLE_NAME,sTitleAndArtistSelection,selectionArgs);
                Log.d("hello","movie has been deleted "+ Deleted);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (Deleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return Deleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SONG:
                return MovieContract.Favourite.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mSongHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case SONG: {
                try {
                    long _id = db.insertOrThrow(MovieContract.Favourite.TABLE_NAME, null, values);
                    if ( _id > 0 )
                        returnUri = MovieContract.Favourite.buildSongUri(_id);
                } catch (SQLiteConstraintException exception) {
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "song"
            case SONG: {
                retCursor = mSongHelper.getReadableDatabase().query(
                        MovieContract.Favourite.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                break;
            }
            // "song/*/*"
            case SONG_AT: {
                retCursor = getSongByArtistTitle(uri, projection, sortOrder);
                break;
            }
            // "search"
            case SEARCH: {
                retCursor = mSongHelper.getReadableDatabase().query(
                        MovieContract.Song.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mSongHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SONG:
                rowsUpdated = db.update(MovieContract.Favourite.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mSongHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SEARCH:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        try {
                            long _id = db.insertOrThrow(MovieContract.Song.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            } } catch (SQLiteConstraintException e) {
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
