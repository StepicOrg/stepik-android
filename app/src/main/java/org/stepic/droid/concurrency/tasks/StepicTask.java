package org.stepic.droid.concurrency.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;


public abstract class StepicTask<Params, Progress, Result> extends AsyncTask<Params, Progress, AsyncResultWrapper<Result>> {


    private ProgressBar mProgressBar;
    protected Context mContext;

    protected StepicTask(Context context) {
        super();
        this.mContext = context;
    }


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

    @SafeVarargs
    @Override
    protected final AsyncResultWrapper<Result> doInBackground(Params... params) {
        try{
            Result result = doInBackgroundBody(params);
            return new AsyncResultWrapper<>(result);
        }
        catch (Throwable exception){
            return new AsyncResultWrapper<>(exception);
        }
    }

    protected abstract Result doInBackgroundBody (Params... params) throws Exception;

    @Override
    protected void onPostExecute(AsyncResultWrapper<Result> resultAsyncResultWrapper) {
        super.onPostExecute(resultAsyncResultWrapper);

        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }

        if (resultAsyncResultWrapper.getException() != null) {
            onException(resultAsyncResultWrapper.getException());
        } else {
            onSuccess(resultAsyncResultWrapper.getResult());
        }
    }

    /**
     * Execute at UI Thread, when task is succeed.
     * @param result of task
     */
    protected void onSuccess(Result result) {
    }

    /**
     * Execute at UI Thread, when task is end with Exception
     * @param exception which was occurred
     */
    protected void onException(Throwable exception) {
    }

    public void unbind() {
        mProgressBar=null;
        mContext = null;
    }
}
