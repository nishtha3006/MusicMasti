package com.example.nishtha.capstone;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nishtha.capstone.Adapters.TopTracksAdapter;
import com.example.nishtha.capstone.Data.MovieContract;
import com.example.nishtha.capstone.Query.FetchTrailers;
import com.example.nishtha.capstone.Query.FetchLyrics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.linearlistview.LinearListView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailSongFragment extends Fragment {
    @Bind(R.id.title) TextView title;
    @Bind(R.id.artistName)TextView artistName;
    @Bind(R.id.lyrics) TextView lyrics;
    @Bind(R.id.fav_button)ImageButton button;
    @Bind(R.id.poster)ImageView poster;
    private AdView mAdView;
    LinearListView trailer_list;
    Song clickedSong =null;
    TopTracksAdapter trailerAdapter;
    ArrayList<Song> songls;
    View view;
    boolean fav;

    public DetailSongFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("hello", "on start");
        if(clickedSong !=null&&Utility.isNetworkAvailable(getContext(),getActivity())) {
            new FetchLyrics(this,getContext()).execute(clickedSong.getTitle(), clickedSong.getArtist());
            new FetchTrailers(this).execute(clickedSong.getArtist());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_detail_song,container,false);
        ButterKnife.bind(this, view);
        trailer_list=(LinearListView)view.findViewById(R.id.detail_trailers);
//        review_list=(LinearListView)view.findViewById(R.id.detail_reviews);
        Bundle arguments=getArguments();
        clickedSong =arguments.getParcelable("song");
        Log.d("hello","this is title"+ clickedSong.getTitle());
        Log.d("hello", "this is artish" + clickedSong.getArtist());
        title.setText(clickedSong.getTitle());
        artistName.setText("Artist : " + clickedSong.getArtist());
        lyrics.setText("Loading...");
        Picasso.with(getContext()).load(clickedSong.getImage_url()).resize(700,750).into(poster);
//        String url = Constants.getMMTrackUrl(title,artist);
//        String response = executeRequest(url);
//        if(clickedSong!=null) {
//            title.setText(clickedSong.getTitle());
//            overview.setText(clickedSong.getOverview());
//            release_date.setText("Release_date : " + clickedSong.getRelease_date());
//            ratings.setText("Ratings :" + Double.toString(clickedSong.getRatings()));
//            Picasso.with(getContext()).load(clickedSong.getImage_url()).resize(700,780).into(poster);
//        }
        button.setSelected(fav);
        button.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_image));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fav = !fav;
                if (clickedSong == null)
                    return;
                button.setSelected(fav);
                setMovieFavoured(fav);
                if (fav) {
                    Toast.makeText(getContext(),
                            "Song has been added to the favourite list", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),
                            "Song has been removed from the favourite list", Toast.LENGTH_LONG).show();
                }
            }
        });

        MobileAds.initialize(getActivity(), getString(R.string.adAppId));

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d("hello","on view created");
        fav=false;
        int present=0;
        present=getContext().getContentResolver().
               query(MovieContract.Favourite.buildTitleArtistUri(clickedSong.getTitle(), clickedSong.getArtist()),
                       null,null,null,null).getCount();
        Log.d("hello",present+" times movie is there ");
        if(present>0){
            fav=true;
            button.setSelected(fav);
        }else
        {
            Log.d("hello","clicked movie is not there in the table");
        }
    }

    public void setMovieFavoured(boolean fav){
        if(fav){
            ContentValues values=new ContentValues();
            values.put(MovieContract.Favourite.COLUMN_TITLE, clickedSong.getTitle());
            values.put(MovieContract.Favourite.COLUMN_ARTIST, clickedSong.getArtist());
            values.put(MovieContract.Favourite.COLUMN_IMAGE_URL, clickedSong.getImage_url());
            getContext().getContentResolver().insert(MovieContract.Favourite.CONTENT_URI, values);

        }else {
            String[] selectionargs = new String[]{clickedSong.getTitle(), clickedSong.getArtist()};
            getContext().getContentResolver().delete
                    (MovieContract.Favourite.buildTitleArtistUri
                            (clickedSong.getTitle(), clickedSong.getArtist()),null,selectionargs);
        }
    }

    public void setTrailerAdapter(Song[] songs){
        songls=new ArrayList<>(Arrays.asList(songs));
        trailerAdapter=new TopTracksAdapter(getContext(),songls);
        trailer_list.setAdapter(trailerAdapter);
        trailer_list.setOnItemClickListener(new LinearListView.OnItemClickListener() {
           @Override
           public void onItemClick(LinearListView parent, View view, int position, long id) {
               Intent intent=new Intent(Intent.ACTION_VIEW);
                Song temp=trailerAdapter.getItem(position);
                intent.setData(Uri.parse(temp.getSong_url()));
                startActivity(intent);
           }
       });
    }



    public void setLyrics(String lyrics) {
        TextView l = (TextView)view.findViewById(R.id.lyrics);
        l.setText(lyrics);
    }
}
