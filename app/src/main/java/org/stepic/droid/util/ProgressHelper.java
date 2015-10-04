package org.stepic.droid.util;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;

public class ProgressHelper {
    public static void dismiss(ProgressBar mProgressLoogin) {

        if (mProgressLoogin != null && mProgressLoogin.getVisibility() != View.GONE) {
            mProgressLoogin.setVisibility(View.GONE);
        }
    }

    public static void activate(ProgressBar progressBar) {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
    }

    public static void dismiss(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    public static void activate(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true);
    }
}
