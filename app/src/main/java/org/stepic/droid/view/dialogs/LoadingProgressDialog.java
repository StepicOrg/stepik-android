package org.stepic.droid.view.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.StringRes;

import org.stepic.droid.R;

public class LoadingProgressDialog extends ProgressDialog {
    public LoadingProgressDialog(Context context, @StringRes int titleRes) {
        super(context);
        setTitle(context.getString(titleRes));
        setMessage(context.getString(R.string.loading_message));
        setCancelable(false);
    }

    public LoadingProgressDialog(Context context) {
        this(context, R.string.loading);
    }

}
