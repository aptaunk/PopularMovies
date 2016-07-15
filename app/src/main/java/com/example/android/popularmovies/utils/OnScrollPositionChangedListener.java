package com.example.android.popularmovies.utils;

import android.view.View;
import android.widget.AbsListView;

    public abstract class OnScrollPositionChangedListener implements AbsListView.OnScrollListener {
        int pos;
        int prevIndex;
        int prevViewPos;
        int prevViewHeight;
        @Override
        public void onScroll(AbsListView v, int i, int vi, int n) {
            try {
                View currView = v.getChildAt(0);
                int currViewPos = Math.round(currView.getTop());
                int diffViewPos = prevViewPos - currViewPos;
                int currViewHeight = currView.getHeight();
                pos += diffViewPos;
                if (i > prevIndex) {
                    pos += prevViewHeight;
                } else if (i < prevIndex) {
                    pos -= currViewHeight;
                }
                prevIndex = i;
                prevViewPos = currViewPos;
                prevViewHeight = currViewHeight;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                onScrollPositionChanged(pos);
            }
        }
        @Override public void onScrollStateChanged(AbsListView absListView, int i) {}
        public abstract void onScrollPositionChanged(int scrollYPosition);
    }