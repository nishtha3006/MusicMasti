package com.example.nishtha.capstone;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.nishtha.capstone.Adapters.GridAdapter;
import com.example.nishtha.capstone.Data.MovieContract;
import com.example.nishtha.capstone.Query.FetchTopTracks;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopSongsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    GridAdapter madapter;
    String country = "india";
    final int SONG_LOADER =0;
    Context mcontext;
    boolean fav;
    String[] projection_movie=new String[]{
            MovieContract.Song.TABLE_NAME+"."+ MovieContract.Song._ID,
            MovieContract.Song.COLUMN_TITLE,
            MovieContract.Song.COLUMN_ARTIST,
            MovieContract.Song.COLUMN_IMAGE_URL
    };
    String[] projection_fav=new String[]{
            MovieContract.Favourite.TABLE_NAME+"."+ MovieContract.Favourite._ID,
            MovieContract.Favourite.COLUMN_TITLE,
            MovieContract.Favourite.COLUMN_ARTIST,
            MovieContract.Favourite.COLUMN_IMAGE_URL
    };

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    public static final int COL_TITLE = 1;
    public static final int COL_ARTIST = 2;
    public static final int COL_IMAGE_URL = 3;

    int no_fav_song =0;

    public TopSongsFragment(){
        country ="popularity";
        mcontext=getContext();
        fav=false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (checkPlayServices()) {

            buildGoogleApiClient();
        }
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
         void onItemSelected(Song song);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            Log.d("hello","getting connected");
            mGoogleApiClient.connect();
        }else{
            updateMovie(country);
        }

    }

    private void updateMovie(String country){
        fav=false;
        if(Utility.isNetworkAvailable(getContext(),getActivity())) {
            FetchTopTracks populateMovie = new FetchTopTracks(getContext());
            populateMovie.execute(country);
            getLoaderManager().restartLoader(SONG_LOADER, null, this);
        }else {
            Toast.makeText(getContext(),"Connection failed",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.song_type,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.sort_by_pop){
            updateMovie(country);
            return  true;
        }
        if(item.getItemId()==R.id.fav){
            fav=true;
            loadFavouriteMovie();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFavouriteMovie(){
        no_fav_song =getContext().getContentResolver()
                .query(MovieContract.Favourite.CONTENT_URI,null,null,null,null).getCount();
        if(no_fav_song ==0){
            Toast.makeText(getContext(),"Favorite list is empty",Toast.LENGTH_LONG).show();
        }else
            getLoaderManager().restartLoader(SONG_LOADER, null, this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        madapter=new GridAdapter(getActivity(),null,0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.grids);
        gridView.setAdapter(madapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor=(Cursor)parent.getItemAtPosition(position);
                Song song =new Song(cursor);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(song);
                }
            }
        });
        return  rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SONG_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader;
        Uri content_uri;
        if(fav){
            loader = new CursorLoader(getActivity(),
                    MovieContract.Favourite.CONTENT_URI,
                    projection_fav,
                    null,
                    null,
                    null);
        }else{
            content_uri= MovieContract.Song.CONTENT_URI;
            loader=new CursorLoader(getActivity(),content_uri,projection_movie,null,null,null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        madapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        madapter.swapCursor(null);
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
        Log.d("hello","in display location");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    country = addresses.get(0).getCountryName();
                }
                no_fav_song =getContext().getContentResolver()
                        .query(MovieContract.Favourite.CONTENT_URI,null,null,null,null).getCount();
                if((fav && no_fav_song ==0)){
                    Toast.makeText(getContext(),"Favorite list is empty",Toast.LENGTH_LONG).show();
                    updateMovie(country);
                }else if(!fav){
                    Log.d("hello","in top songs");
                    updateMovie(country);
                }
            } catch (IOException ignored) {
                //do something
            }
        } else {

        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }


    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("hello", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
}
