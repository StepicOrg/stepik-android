package org.stepic.droid.util;

import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.stepic.droid.ui.dialogs.LoadingProgressDialog;

public class ProgressHelper {
    public static void dismiss(ProgressBar mProgressLogin) {

        if (mProgressLogin != null && mProgressLogin.getVisibility() != View.GONE) {
            mProgressLogin.setVisibility(View.GONE);
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

    public static void activate(LoadingProgressDialog progressDialog) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public static void dismiss(LoadingProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception ignored) {
            }
        }
    }

    public static void activate(DialogFragment progressDialog, FragmentManager fragmentManager, String tag) {
        if (progressDialog != null && !progressDialog.isAdded() && fragmentManager.findFragmentByTag(tag) == null) {
            progressDialog.show(fragmentManager, tag);
        }
    }

    public static void dismiss(FragmentManager fragmentManager, String tag) {
        if (fragmentManager != null) {
            try {
                DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(tag);
                fragment.dismiss();
            } catch (Exception ignored) {
            }
        }
    }
}
