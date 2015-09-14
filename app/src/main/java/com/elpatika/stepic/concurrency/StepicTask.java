package com.elpatika.stepic.concurrency;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.elpatika.stepic.core.IShell;

import javax.inject.Inject;


public abstract class StepicTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {


    @Inject
    IShell mShell;
    private ProgressBar mProgressBar;
//    protected final Handler handler = new Handler();
    protected Context mContext;

    protected StepicTask(Context context) {
        super();
        this.mContext = context;
    }

//
//    protected void handle(final Exception ex) {
//        handler.post(new Runnable() {
//            public void run() {
//                //todo: clarify exception
//                onException(ex);
//            }
//        });
//    }

    public void setProgressBar(ProgressBar progressBar) {
        this.mProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
