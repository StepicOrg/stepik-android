package com.elpatika.stepic.concurrency;

import android.content.Context;
import android.os.Handler;
import android.widget.ProgressBar;

import roboguice.util.RoboAsyncTask;

public abstract class StepicTask<T>  extends RoboAsyncTask<T> {


    private ProgressBar mProgressBar;
    protected final Handler handler = new Handler();

    protected StepicTask(Context context) {
        super(context);
    }



    protected void handle(final Exception ex) {
        handler.post(new Runnable() {
            public void run() {
                //todo: clarify exception
                onException(ex);
            }
        });
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.mProgressBar = progressBar;
    }
}
