package com.example.louissankey.popularmoviestablet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.louissankey.popularmoviestablet.database.MoviesDatabaseHandler;
import com.example.louissankey.popularmoviestablet.model.Movie;
import com.example.louissankey.popularmoviestablet.model.MoviePosterAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.gridview)
    GridView mGridView;
    @Bind(R.id.main_activity_header)
    TextView mMainActivityHeaderTextView;

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String MOVIE_TITLE = "MOVIE_TITLE";
    public static final String MOVIE_POSTER_URL = "MOVIE_POSTER_URL";
    public static final String MOVIE_OVERVIEW = "MOVIE_OVERVIEW";
    public static final String MOVIE_VOTE_AVERAGE = "MOVIE_VOTE_AVERAGE";
    public static final String MOVIE_ID = "MOVIE_ID";
    public static final String FAVORITE_MOVIES = "FAVORITE_MOVIES";
    public static final String RELEASE_DATE = "RELEASE_DATE";
    public static final String IS_CHECKED = "IS_CHECKED";

    private String byPopularityUrl = "&sort_by=popularity.desc";
    private String byHighestRatedUrl = "&sort_by=vote_average.desc";

    private List<Movie> movieList;
    private List<Movie> favoriteMoviesList;
    private String jsonData;
    private MoviePosterAdapter moviePosterAdapter;

    //provided so when user updates favorites they can navigate directly back to "favorites" via back button and see update via onActivityResult method
    private int mSettingForResult;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        ButterKnife.bind(this);


        if (savedInstanceState != null) {
            mMainActivityHeaderTextView.setText(savedInstanceState.getString("HEADER_LABEL"));
            movieList = (List<Movie>) savedInstanceState.get("MOVIE_LIST");
            moviePosterAdapter = new MoviePosterAdapter(MainActivity.this, movieList);
            mGridView.setAdapter(moviePosterAdapter);



        } else {

            getMovieJson(byPopularityUrl);
            mSettingForResult = 0;
            movieList = new ArrayList<>();
        }

        if (findViewById(R.id.movie_detail_container) != null) {

            mTwoPane = true;
        }



        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Movie movie = movieList.get(position);
                Bundle bundle = new Bundle();
                bundle.putString(MOVIE_TITLE, movie.getTitle());
                bundle.putString(MOVIE_POSTER_URL, movie.getPosterUrl());
                bundle.putString(MOVIE_OVERVIEW, movie.getOverview());
                bundle.putDouble(MOVIE_VOTE_AVERAGE, movie.getVoteAverage());
                bundle.putInt(MOVIE_ID, movie.getMovieId());
                bundle.putString(RELEASE_DATE, movie.getReleaseDate());

                favoriteMoviesList = getFavoriteMoviesList();

                if (favoriteMoviesList != null) {
                    for (Movie favoriteMovie : favoriteMoviesList) {

                        if (favoriteMovie.getMovieId() == movie.getMovieId()) {
                            bundle.putBoolean(IS_CHECKED, true);
                            break;
                        } else {
                            bundle.putBoolean(IS_CHECKED, false);
                        }
                    }
                }



                if (mTwoPane) {

                    DetailFragment fragment = new DetailFragment();
                    fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
                    Log.v(TAG, "two pane");
                    Log.v(TAG, movie.getTitle());
                } else {

                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, mSettingForResult);
                    Log.v(TAG, "single pane");
                    Log.v(TAG, movie.getTitle());
                }



            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_CANCELED) {
                movieList = getFavoriteMoviesList();
                moviePosterAdapter = new MoviePosterAdapter(MainActivity.this, movieList);
                mGridView.setAdapter(moviePosterAdapter);

            }
        }
    }


    public void getMovieJson(final String sortUrl) {

        String apiKey = BuildConfig.POPULAR_MOVIES_API_KEY;

        Request request = new Request.Builder()
                .url(getString(R.string.themovedb_base_url) + apiKey + sortUrl)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                movieList.clear();

                jsonData = response.body().string();
                Log.v(TAG, jsonData);
                JSONObject movieJson;

                try {
                    movieJson = new JSONObject(jsonData);
                    JSONArray results = movieJson.getJSONArray(getString(R.string.json_results_key));
                    for (int i = 0; i < results.length(); i++) {
                        Movie movie = new Movie();
                        JSONObject movieObject = results.getJSONObject(i);
                        movie.setPosterUrl(movieObject.getString(getString(R.string.json_poster_path_key)));
                        movie.setTitle(movieObject.getString(getString(R.string.json_original_title_key)));
                        movie.setOverview(movieObject.getString(getString(R.string.json_overview_key)));
                        movie.setReleaseDate(movieObject.getString(getString(R.string.json_release_date_key)));
                        movie.setVoteAverage(movieObject.getDouble(getString(R.string.json_vote_average_key)));
                        movie.setMovieId(movieObject.getInt(getString(R.string.id)));

                        movieList.add(movie);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                moviePosterAdapter = new MoviePosterAdapter(MainActivity.this, movieList);

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mGridView.setAdapter(moviePosterAdapter);

                        if (sortUrl.equals(byPopularityUrl)) {
                            mMainActivityHeaderTextView.setText(R.string.main_activity_header_most_popular);
                        } else if (sortUrl.equals(byHighestRatedUrl)) {
                            mMainActivityHeaderTextView.setText(R.string.main_activity_header_highest_rated);
                        }
                    }
                });
            }
        });

    }

    //todo: may not need own method
    private List<Movie> getFavoriteMoviesList() {
        MoviesDatabaseHandler db = new MoviesDatabaseHandler(this, null, null, 1);
        List<Movie> movies = db.getAllMovies();
        return movies;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelableArrayList("MOVIE_LIST", (ArrayList<Movie>) movieList);
        savedInstanceState.putString("HEADER_LABEL", mMainActivityHeaderTextView.getText().toString());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_popularity:
                getMovieJson(byPopularityUrl);
                mSettingForResult = 0;
                break;
            case R.id.action_ratings:
                getMovieJson(byHighestRatedUrl);
                mSettingForResult = 0;
                break;
            case R.id.action_favorites:
                favoriteMoviesList = getFavoriteMoviesList();

                if (favoriteMoviesList != null) {

                    movieList.clear();
                    movieList = favoriteMoviesList;
                    moviePosterAdapter = new MoviePosterAdapter(MainActivity.this, movieList);
                    mGridView.setAdapter(moviePosterAdapter);
                    mMainActivityHeaderTextView.setText(R.string.favorite_movies);
                    mSettingForResult = 1;

                } else {
                    Toast.makeText(MainActivity.this, "You have no movies saved to your favorites.", Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
