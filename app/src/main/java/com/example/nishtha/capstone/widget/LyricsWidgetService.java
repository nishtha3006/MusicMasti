package com.example.nishtha.capstone.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;


public class LyricsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new LyricsViews(this);
    }
}
