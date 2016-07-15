package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.utils.HttpUtils;
import com.example.android.popularmovies.utils.JsonUtils;
import com.example.android.popularmovies.utils.UriUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.gridview_movies) GridView gridView;
    @BindView(R.id.main_toolbar) Toolbar mainToolbar;

    JsonUtils jsonUtils;
    UriUtils uriUtils;

    private boolean isSortByPopularity = true;
    ArrayAdapter<String> adapter;
    private int pageNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jsonUtils = new JsonUtils();
        uriUtils = new UriUtils(getResources());
        ButterKnife.bind(this);
        setSupportActionBar(mainToolbar);

        //Initialize the adapter
        adapter = new ArrayAdapter<String>(this,0) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                //Create new ImageView if it doesn't exist
                ImageView view = (ImageView) convertView;
                if (view == null) {
                    view = new ImageView(MainActivity.this);
                }

                //Set the ImageView's width and height
                float newHeight = getResources().getFraction(R.fraction.poster_height_to_width_ratio,gridView.getColumnWidth(),1);
                view.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT,Math.round(newHeight)));

                //Set padding around posters in the grid
                int padding = Math.round(getResources().getDimension(R.dimen.poster_padding));
                view.setPadding(padding,padding,padding,padding);

                //Place the poster image inside the view
                jsonUtils.setMovie(getItem(position));
                Glide.with(MainActivity.this)
                    .load(uriUtils.getPosterUri(jsonUtils.getPosterPath()))
                    .into(view);

                return view;

            }
        };

        //Restore app to a previous state
        if (savedInstanceState != null) {
            //Restore all items in the adapter
            adapter.addAll(savedInstanceState.getStringArrayList("Adapter Items"));
            //Restore all the position in the gridview
            gridView.smoothScrollToPosition(savedInstanceState.getInt("GridView Position"));
            //Restore the sort by method
            isSortByPopularity = savedInstanceState.getBoolean("Sort By Popularity");
            //Restore the index of the last page that was loaded
            pageNumber = savedInstanceState.getInt("Page Number");
        }

        //Attach the adapter to the gridview
        gridView.setAdapter(adapter);

        //When a poster is clicked, launch the detail activity
        gridView.setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String movieData = adapterView.getItemAtPosition(i).toString();
                    Intent intent = new Intent(MainActivity.this,DetailActivity.class)
                        .putExtra("Movie Data",movieData);
                    startActivity(intent);
                }
            }
        );

        //Allow endless scrolling
        gridView.setOnScrollListener(
            new GridView.OnScrollListener() {
                int prevNumItems = 0;
                @Override
                public void onScroll(AbsListView v, int i, int vi, int n) {
                    //Load the new page only if no other pages have been loaded
                    //or the adapter has loaded new items and user has scrolled to the end of the list
                    if (pageNumber == 0 || (n>prevNumItems && i+vi>=n)) {
                        prevNumItems = n;
                        loadPage(++pageNumber);
                    }
                }
                @Override
                public void onScrollStateChanged(AbsListView v, int i) {}
            }
        );

    }

    //Save all relevant info to recover from an orientation change etc
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> adapterItems = new ArrayList<>();
        for(int i=0 ; i<adapter.getCount() ; i++){
            adapterItems.add(adapter.getItem(i));
        }
        outState.putInt("Page Number",pageNumber);
        outState.putBoolean("Sort By Popularity",isSortByPopularity);
        outState.putStringArrayList("Adapter Items",adapterItems);
        outState.putInt("GridView Position",gridView.getFirstVisiblePosition());
    }

    //Adds menu items (sort by method) in activity_main_menu to the main activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    //On clicking a menu item, change the sorting order and refresh the page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_pop:
                isSortByPopularity = true;
                adapter.clear();
                pageNumber = 0;
                return true;
            case R.id.sort_by_rating:
                isSortByPopularity = false;
                adapter.clear();
                pageNumber = 0;
                return true;
            case R.id.view_favorites:
                adapter.clear();
                pageNumber = 0;
                loadFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Adds more movie data to the adapter given the page number
    //using an AsyncTask that updates the adapter in the background
    private void loadPage(final int page) {
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... in) {
                return HttpUtils.getResponse(uriUtils.getMoviesUri(isSortByPopularity,page));
            }
            @Override
            protected void onPostExecute(String jsonString) {
                adapter.addAll(JsonUtils.getResults(jsonString));
            }
        }.execute();
    }

    private void loadFavorites() {
        Log.e("FAVORITES","Loading Favorites");
        Set<String> favoriteMovies = getPreferences(MODE_PRIVATE).getStringSet("Favorite Movie IDs",new TreeSet<String>());
        Log.e("FAVORITES",""+favoriteMovies.size());
        String[] favoriteMoviesArray = new String[favoriteMovies.size()];
        int i=0;
        for (String movieId : favoriteMovies) {
            Uri favoriteMovieUri = Uri.parse(getResources().getString(R.string.api_base_url))
                .buildUpon()
                .appendPath(movieId)
                .appendQueryParameter("api_key",getResources().getString(R.string.api_key))
                .build();
            Log.e("FAVORITES",favoriteMovieUri.toString());
            String jsonFavoriteMovie = HttpUtils.getResponse(favoriteMovieUri.toString());
            Log.e("FAVORITES",jsonFavoriteMovie);
            favoriteMoviesArray[i] = jsonFavoriteMovie;
            i++;
        }
        adapter.addAll(favoriteMoviesArray);
    }



}
