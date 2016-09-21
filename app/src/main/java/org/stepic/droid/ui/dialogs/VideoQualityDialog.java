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

    private Map<String, Integer> qualityToPositionMap = null;
    private Map<Integer, String> positionToQualityMap = null;

    static {
    }


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);
        if (qualityToPositionMap == null || positionToQualityMap == null) {
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
                        qualityToPositionMap.get(userPreferences.getQualityVideo()),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                analytic.reportEventWithIdName(Analytic.Preferences.VIDEO_QUALITY, which + "", positionToQualityMap.get(which));
                                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        userPreferences.storeQualityVideo(positionToQualityMap.get(which));
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

        if (qualityToPositionMap == null) {
            qualityToPositionMap = new HashMap<>();
            qualityToPositionMap.put("270", 0);
            qualityToPositionMap.put("360", 1);
            qualityToPositionMap.put("720", 2);
            qualityToPositionMap.put("1080", 3);
        }

        if (positionToQualityMap == null) {
            positionToQualityMap = new HashMap<>();
            positionToQualityMap.put(0, "270");
            positionToQualityMap.put(1, "360");
            positionToQualityMap.put(2, "720");
            positionToQualityMap.put(3, "1080");
        }
    }
}
