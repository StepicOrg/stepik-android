package org.stepic.droid.view.custom;

import android.app.ProgressDialog;
import android.content.Context;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;

public class LoadingProgressDialog extends ProgressDialog {
    public LoadingProgressDialog(Context context) {
        super(context);
        setTitle(MainApplication.getAppContext().getString(R.string.loading));
        setMessage(MainApplication.getAppContext().getString(R.string.loading_message));
        setCancelable(false);
    }

}
