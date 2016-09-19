package org.stepic.droid.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class VideoQualityDialog extends DialogFragment {


    @Inject
    IShell shell;

    @Inject
    DatabaseFacade databaseFacade;

    @Inject
    UserPreferences userPreferences;

    @Inject
    Analytic analytic;

    private Map<String, Integer> mQualityToPositionMap = null;
    private Map<Integer, String> mPositionToQualityMap = null;

    static {
    }


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);
        if (mQualityToPositionMap == null || mPositionToQualityMap == null) {
            initMaps();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.video_quality)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        analytic.reportEvent(Analytic.Interaction.CANCEL_VIDEO_QUALITY);
                    }
                })
                .setSingleChoiceItems(R.array.video_quality,
                        mQualityToPositionMap.get(userPreferences.getQualityVideo()),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                analytic.reportEventWithIdName(Analytic.Preferences.VIDEO_QUALITY, which + "", mPositionToQualityMap.get(which));
                                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        userPreferences.storeQualityVideo(mPositionToQualityMap.get(which));
                                        return null;
                                    }
                                };
                                task.execute();
                                dialog.dismiss();
                            }
                        });

        return builder.create();
    }

    private void initMaps() {

        if (mQualityToPositionMap == null) {
            mQualityToPositionMap = new HashMap<>();
            mQualityToPositionMap.put("270", 0);
            mQualityToPositionMap.put("360", 1);
            mQualityToPositionMap.put("720", 2);
            mQualityToPositionMap.put("1080", 3);
        }

        if (mPositionToQualityMap == null) {
            mPositionToQualityMap = new HashMap<>();
            mPositionToQualityMap.put(0, "270");
            mPositionToQualityMap.put(1, "360");
            mPositionToQualityMap.put(2, "720");
            mPositionToQualityMap.put(3, "1080");
        }
    }
}
