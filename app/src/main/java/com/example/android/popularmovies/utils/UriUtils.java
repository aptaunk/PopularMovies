package com.example.android.popularmovies.utils;


import android.content.res.Resources;
import android.net.Uri;

import com.example.android.popularmovies.R;

public class UriUtils {

    private Resources r;

    public UriUtils(Resources r) {
        this.r = r;
    }

    public String getMoviesUri(boolean isSortByPopularity, int page) {
        String sortByMethod = isSortByPopularity?"popular":"top_rated";
        return Uri.parse(r.getString(R.string.api_base_url))
            .buildUpon()
            .appendPath(sortByMethod)
            .appendQueryParameter("api_key", r.getString(R.string.api_key))
            .appendQueryParameter("page", String.valueOf(page))
            .build().toString();
    }

    public String getPosterUri(String posterPath) {
        return Uri.parse(r.getString(R.string.poster_base_url))
            .buildUpon()
            .appendPath(r.getString(R.string.poster_quality))
            .appendPath(posterPath.substring(1))
            .build().toString();
    }

    public String getBackdropUri(String backdropPath) {
        return Uri.parse(r.getString(R.string.poster_base_url))
            .buildUpon()
            .appendPath(r.getString(R.string.backdrop_quality))
            .appendPath(backdropPath.substring(1))
            .build().toString();
    }

    public String getTrailersUri(int movieId) {
        return Uri.parse(r.getString(R.string.api_base_url))
            .buildUpon()
            .appendPath(String.valueOf(movieId))
            .appendPath("videos")
            .appendQueryParameter("api_key", r.getString(R.string.api_key))
            .appendQueryParameter("site", "YouTube")
            .build().toString();
    }

    public String getTrailerThumbnailUri(String key) {
        return Uri.parse(r.getString(R.string.trailer_thumbnail_base_url))
            .buildUpon()
            .appendPath(key)
            .appendPath("0.jpg")
            .build().toString();
    }

    public Uri getTrailerVideoUri(String key) {
        return Uri.parse(r.getString(R.string.trailer_video_base_url))
            .buildUpon()
            .appendQueryParameter("v",key)
            .build();
    }

    public String getReviewsUri(int movieId, int page) {
        return Uri.parse(r.getString(R.string.api_base_url))
            .buildUpon()
            .appendPath(String.valueOf(movieId))
            .appendPath("reviews")
            .appendQueryParameter("api_key", r.getString(R.string.api_key))
            .appendQueryParameter("page", String.valueOf(page))
            .build().toString();
    }

    public Uri getReviewUri(String reviewUrl) {
        return Uri.parse(reviewUrl);
    }

}
