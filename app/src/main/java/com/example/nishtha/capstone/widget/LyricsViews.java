package com.example.nishtha.capstone.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.example.nishtha.capstone.Data.MovieContract;
import com.example.nishtha.capstone.Song;
import com.example.nishtha.capstone.TopSongsFragment;
import com.example.nishtha.capstone.R;


public class LyricsViews implements RemoteViewsService.RemoteViewsFactory {
    private Cursor cursor;
    private Context mcontext;

    public LyricsViews(Context mcontext) {
        this.mcontext = mcontext;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (cursor != null) {
            cursor.close();
        }

        // URI for favourite data items
//        getContext().getContentResolver()
//                .query(MovieContract.Favourite.CONTENT_URI,null,null,null,null)
        Uri uri = MovieContract.Favourite.CONTENT_URI;

        cursor = mcontext.getContentResolver().query(uri, null, null, null, null);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (cursor == null) {
            return 0;
        } else {
            return cursor.getCount();
        }
    }

    @Override
    public RemoteViews getViewAt(int i) {
        if (!cursor.moveToPosition(i)) {
            return null;
        }


        RemoteViews views = new RemoteViews(mcontext.getPackageName(), R.layout.widget_list_item);
        views.setTextViewText(R.id.title, cursor.getString(TopSongsFragment.COL_TITLE));
        views.setTextViewText(R.id.artist, cursor.getString(TopSongsFragment.COL_ARTIST));

        Song song =  new Song(cursor);
        Intent intent=new Intent();
        intent.putExtra("song", song);
        views.setOnClickFillInIntent(R.id.list_item, intent);
        return views;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }


}
