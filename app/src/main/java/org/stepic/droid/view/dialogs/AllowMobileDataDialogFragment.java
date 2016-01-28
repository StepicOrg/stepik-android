package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.squareup.otto.Bus;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.wifi_settings.WifiLoadIsChangedEvent;

import javax.inject.Inject;

public class AllowMobileDataDialogFragment extends DialogFragment {
    @Inject
    Bus mBus;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.allow_mobile_download_title)
                .setMessage(R.string.allow_mobile_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //mobile allowed
                        mBus.post(new WifiLoadIsChangedEvent(true));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //only wifi allowed
                        mBus.post(new WifiLoadIsChangedEvent(false));
                    }
                });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d("AllowMobile", "onCancel");
        mBus.post(new WifiLoadIsChangedEvent(false));
    }
}