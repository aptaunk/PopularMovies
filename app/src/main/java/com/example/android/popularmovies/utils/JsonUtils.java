package com.example.android.popularmovies.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    private JSONObject movie;
    private JSONObject trailer;
    private JSONObject review;

    public static String[] getResults(String jsonData) {
        try {
            JSONArray ja = new JSONObject(jsonData).getJSONArray("results");
            String[] results = new String[ja.length()];
            for (int i = 0; i < ja.length(); i++) {
                results[i] = ja.getJSONObject(i).toString();
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setMovie(String jsonData) {
        try {
            this.movie = new JSONObject(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTrailer(String jsonData) {
        try {
            this.trailer = new JSONObject(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setReview(String jsonData) {
        try {
            this.review = new JSONObject(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPosterPath() {
        try {
            return movie.getString("poster_path");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getOverview() {
        try {
            return movie.getString("overview");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getReleaseDate() {
        try {
            return movie.getString("release_date");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getMovieId() {
        try {
            return movie.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getMovieTitle() {
        try {
            return movie.getString("title");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBackdropPath() {
        try {
            return movie.getString("backdrop_path");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getRating() {
        try {
            return movie.getDouble("vote_average");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getTrailerKey() {
        try {
            return trailer.getString("key");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTrailerName() {
        try {
            return trailer.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getReviewAuthor() {
        try {
            return review.getString("author");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getReviewContent() {
        try {
            return review.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getReviewUrl() {
        try {
            return review.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
