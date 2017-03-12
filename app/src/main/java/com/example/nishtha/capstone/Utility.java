package com.example.nishtha.capstone;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utility {


    public static boolean isNetworkAvailable(Context context,Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)activity.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
