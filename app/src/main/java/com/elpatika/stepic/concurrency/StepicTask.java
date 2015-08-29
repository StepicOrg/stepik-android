package com.elpatika.stepic.concurrency;

import android.content.Context;

import roboguice.util.RoboAsyncTask;

public class StepicTask<T>  extends RoboAsyncTask<T> {

    protected StepicTask(Context context) {
        super(context);
    }

    @Override
    public T call() throws Exception {
        return null;
    }
}
