package com.example.louissankey.popularmoviestablet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.louissankey.popularmoviestablet.database.MoviesDatabaseHandler;
import com.example.louissankey.popularmoviestablet.model.Movie;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailFragment extends Fragment {


    @Bind(R.id.title_details)
    TextView titleDetailsTextView;
    @Bind(R.id.overview_details)
    TextView overveiewDetailsTextView;
    @Bind(R.id.movie_details_imageview)
    ImageView movieDetailsImageView;
    @Bind(R.id.votes_details)
    TextView votesDetailsTextView;
    @Bind(R.id.author_name)
    TextView authorNameTextView;
    @Bind(R.id.reviews_textview)
    TextView reviewsTextView;
    @Bind(R.id.review_header_label)
    TextView reviewsHeaderLAbel;
    @Bind(R.id.author_label)
    TextView authorLabel;
    @Bind(R.id.favorite_checkBox)
    CheckBox favoriteCheckbox;
    @Bind(R.id.releaseDateTextView)
    TextView releaseDateTextView;


    public static final String TAG = DetailFragment.class.getSimpleName();
    public static final String MOVIE_TRAILER_KEY = "MOVIE_TRAILER_KEY";

    private int mMovieId;
    private String mMovieTitle;
    private String mMoviePosterUrl;
    private String mMovieOverview;
    private String mMovieReleaseDate;
    private Double mMovieVoteAverage;
    private Boolean mIsChecked;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle extras = getArguments();
        mMovieId = extras.getInt(MainActivity.MOVIE_ID);
        mMovieTitle = extras.getString(MainActivity.MOVIE_TITLE);
        mMoviePosterUrl = extras.getString(MainActivity.MOVIE_POSTER_URL);
        mMovieOverview = extras.getString(MainActivity.MOVIE_OVERVIEW);
        mMovieReleaseDate = extras.getString(MainActivity.RELEASE_DATE);
        mMovieVoteAverage = extras.getDouble(MainActivity.MOVIE_VOTE_AVERAGE);
        mIsChecked = extras.getBoolean(MainActivity.IS_CHECKED);

            Activity activity = this.getActivity();
            Toolbar appBarLayout = (Toolbar) activity.findViewById(R.id.detail_toolbar);
            if (appBarLayout != null) {

                appBarLayout.setTitle(appBarLayout.getTitle());

            }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        if (mIsChecked) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    favoriteCheckbox.setChecked(true);
                }
            };
            runnable.run();
        }

        Picasso.with(getActivity())
                .load(getString(R.string.image_path_prefix_url) + mMoviePosterUrl)
                .into(movieDetailsImageView);


        titleDetailsTextView.setText(mMovieTitle);

        overveiewDetailsTextView.setText(mMovieOverview);
        votesDetailsTextView.setText(NumberFormat.getInstance().format(mMovieVoteAverage));
        releaseDateTextView.setText(mMovieReleaseDate);

        favoriteCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                MoviesDatabaseHandler db = new MoviesDatabaseHandler(getActivity(), null, null, 1);

                if (isChecked) {
                    db.addMovie(new Movie(mMovieId, mMovieTitle, mMoviePosterUrl, mMovieOverview, mMovieReleaseDate, mMovieVoteAverage));
                } else {

                    Movie movie = db.getMovie(mMovieId);
                    db.deleteMovie(movie.getMovieId());
                }

            }
        });

        showMovieTrailerFragment();
        showReviews();



        return rootView;
    }

    public void showReviews() {
        String apiKey = BuildConfig.POPULAR_MOVIES_API_KEY;

        final Request request = new Request.Builder()
                .url(getString(R.string.themovedb_video_url) + mMovieId + getString(R.string.reviews_url_part_two) + apiKey)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData;
                jsonData = response.body().string();
                Log.v(TAG, jsonData);
                JSONObject movieJson;

                try {

                    movieJson = new JSONObject(jsonData);
                    final JSONArray results = movieJson.getJSONArray(getString(R.string.json_results_key));
                    final JSONObject reviewObject = results.getJSONObject(0);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                reviewsHeaderLAbel.setText(R.string.reviews);
                                authorLabel.setText(R.string.author);
                                authorNameTextView.setText(reviewObject.getString("author"));
                                reviewsTextView.setText(reviewObject.getString("content"));


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    }


    public void showMovieTrailerFragment() {

        String apiKey = BuildConfig.POPULAR_MOVIES_API_KEY;

        final Request request = new Request.Builder()
                .url(getString(R.string.themovedb_video_url) + mMovieId + getString(R.string.video_url_part_two) + apiKey)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData;
                jsonData = response.body().string();
                Log.v(TAG, jsonData);
                JSONObject movieJson;
                String movieTrailerKey;

                try {

                    movieJson = new JSONObject(jsonData);
                    JSONArray results = movieJson.getJSONArray(getString(R.string.json_results_key));

                    MovieTrailerFragment fragment = new MovieTrailerFragment();
                    Bundle args = new Bundle();

                    //check if trailer is available
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject jsonObject = results.getJSONObject(i);
                        if (jsonObject.getString(getString(R.string.type)).equals(getString(R.string.trailer))) {
                            movieTrailerKey = jsonObject.getString("key");

                            //if trailer key is available, bundle it and start fragment
                            args.putString(MOVIE_TRAILER_KEY, movieTrailerKey);
                            fragment.setArguments(args);

                            //todo: on slow connection user may navigate back before commit and crash app
                            FragmentManager manager = getFragmentManager();
                            manager.beginTransaction()
                                    .replace(R.id.frame_layout, fragment)
                                    .commit();

                            break;

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

}
