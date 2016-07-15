package com.example.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.utils.HttpUtils;
import com.example.android.popularmovies.utils.JsonUtils;
import com.example.android.popularmovies.utils.OnScrollPositionChangedListener;
import com.example.android.popularmovies.utils.UriUtils;
import com.squareup.picasso.Picasso;

import java.util.Set;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.detail_toolbar) Toolbar detailToolbar;
    @BindView(R.id.reviews_listview) ListView reviewsListView;
    @BindView(R.id.backdrop_imageview) ImageView backdropImageView;
    //@BindView(R.id.review_empty_textview) TextView reviewEmptyTextView;

    private JsonUtils jsonUtils;
    private UriUtils uriUtils;

    private String[] trailers;
    private RecyclerView.Adapter<TrailerViewHolder> trailerAdapter;
    private ArrayAdapter<String> reviewAdapter;
    private int pageNumber = 0;
    private DetailHeaderViewHolder dh;

    public class DetailHeaderViewHolder {
        //@BindView(R.id.movie_title_textview) TextView movieTitleTextView;
        @BindView(R.id.release_date_textview) TextView releaseDateTextView;
        @BindView(R.id.rating_textview) TextView ratingTextView;
        @BindView(R.id.overview_textview) TextView overviewTextView;
        @BindView(R.id.trailers_heading_textview) TextView trailersHeadingTextView;
        @BindView(R.id.trailers_recyclerview) RecyclerView trailersRecyclerView;
        @BindView(R.id.reviews_heading_textview) TextView reviewsHeadingTextView;
        public DetailHeaderViewHolder(View v) {
            ButterKnife.bind(this,v);
        }
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.trailer_imageview) ImageView trailerImg;
        @BindView(R.id.trailer_textview) TextView trailerName;
        public TrailerViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
        }
    }

    public class ReviewViewHolder {
        @BindView(R.id.review_content_textview) TextView reviewContentTextView;
        @BindView(R.id.review_author_textview) TextView reviewAuthorTextView;
        public ReviewViewHolder(View v) {
            ButterKnife.bind(this,v);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        jsonUtils = new JsonUtils();
        jsonUtils.setMovie(getIntent().getStringExtra("Movie Data"));
        uriUtils = new UriUtils(getResources());

        ButterKnife.bind(this);
        detailToolbar.setTitle(jsonUtils.getMovieTitle());
        setSupportActionBar(detailToolbar);

        View detailHeaderView = getLayoutInflater().inflate(R.layout.detail_header_layout, reviewsListView,false);
        dh = new DetailHeaderViewHolder(detailHeaderView);

        trailerAdapter = new RecyclerView.Adapter<TrailerViewHolder>() {
            @Override
            public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(DetailActivity.this).inflate(R.layout.trailer_layout,null);
                return new TrailerViewHolder(v);
            }
            @Override
            public void onBindViewHolder(TrailerViewHolder holder, int position) {
                jsonUtils.setTrailer(trailers[position]);
                Picasso.with(DetailActivity.this)
                    .load(uriUtils.getTrailerThumbnailUri(jsonUtils.getTrailerKey()))
                    .into(holder.trailerImg);
                holder.trailerName.setText(jsonUtils.getTrailerName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW, uriUtils.getTrailerVideoUri(jsonUtils.getTrailerKey())));
                    }
                });
            }
            @Override
            public int getItemCount() {
                if (trailers!=null) {
                    return trailers.length;
                }
                return 0;
            }
        };
        dh.trailersRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        dh.trailersRecyclerView.setAdapter(trailerAdapter);

        reviewsListView.addHeaderView(detailHeaderView);
        reviewAdapter = new ArrayAdapter<String>(this,R.layout.review_layout){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.review_layout,null,false);
                    convertView.setTag(new ReviewViewHolder(convertView));
                }
                ReviewViewHolder h = (ReviewViewHolder)convertView.getTag();
                jsonUtils.setReview(getItem(position));
                h.reviewContentTextView.setText(jsonUtils.getReviewContent());
                h.reviewAuthorTextView.setText(jsonUtils.getReviewAuthor());
                return convertView;
            }
        };
        reviewsListView.setAdapter(reviewAdapter);
        reviewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                jsonUtils.setReview(adapterView.getItemAtPosition(i).toString());
                startActivity(new Intent(Intent.ACTION_VIEW,uriUtils.getReviewUri(jsonUtils.getReviewUrl())));
            }
        });
        reviewsListView.setOnScrollListener(
            new OnScrollPositionChangedListener() {
                int prevNumItems = 0;
                @Override
                public void onScroll(AbsListView v, int i, int vi, int n) {
                    super.onScroll(v, i, vi, n);
                    if (pageNumber == 0 || (n>prevNumItems && i+vi>=n)) {
                        prevNumItems = n;
                        loadReviews(++pageNumber);
                    }
                }
                @Override
                public void onScrollPositionChanged(int scrollYPosition) {
                    //Parallax effect for the backdrop
                    backdropImageView.setTranslationY(-scrollYPosition/5);
                }
            }
        );

        //Populate each view with the appropriate data passed through the intent
        Picasso.with(this)
            .load(uriUtils.getBackdropUri(jsonUtils.getBackdropPath()))
            .into(backdropImageView);

        //dh.movieTitleTextView.setText(jsonUtils.getMovieTitle());
        dh.releaseDateTextView.setText(jsonUtils.getReleaseDate());
        dh.ratingTextView.setText(String.valueOf(jsonUtils.getRating()));
        dh.overviewTextView.setText(jsonUtils.getOverview());
        loadTrailers();
    }

    //Adds menu items (sort by method) in activity_main_menu to the main activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_menu, menu);
        return true;
    }

    //On clicking a menu item, change the sorting order and refresh the page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_to_favorites:
                addToFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addToFavorites() {
        Log.e("FAVORITES","adding to favorites");
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        TreeSet<String> favSet = new TreeSet<String>();
        favSet.add(String.valueOf(jsonUtils.getMovieId()));
        sp.edit().putStringSet("Favorite Movie IDs",favSet).apply();
    }

    //gets the json data regarding available trailers for this movie
    private void loadTrailers() {
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... in) {
                return HttpUtils.getResponse(uriUtils.getTrailersUri(jsonUtils.getMovieId()));
            }
            @Override
            protected void onPostExecute(String jsonString) {
                trailers = JsonUtils.getResults(jsonString);
                trailerAdapter.notifyDataSetChanged();
                if (trailers == null || trailers.length == 0) {
                    dh.trailersHeadingTextView.setVisibility(View.GONE);
                    dh.trailersRecyclerView.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    //gets the json data regarding available reviews for this movie
    private void loadReviews(final int page) {
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... in) {
                return HttpUtils.getResponse(uriUtils.getReviewsUri(jsonUtils.getMovieId(),page));
            }
            @Override
            protected void onPostExecute(String jsonString) {
                reviewAdapter.addAll(JsonUtils.getResults(jsonString));
                if (reviewAdapter.isEmpty()) {
                    dh.reviewsHeadingTextView.setVisibility(TextView.GONE);
                }
            }
        }.execute();
    }

}
