package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.squareup.otto.Bus;
import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.loading.FinishLoadEvent;
import org.stepic.droid.events.loading.StartLoadEvent;
import org.stepic.droid.events.video.VideosMovedEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.StorageUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

public class WantMoveDataDialog extends DialogFragment {
    public static DialogFragment newInstance() {
        return new WantMoveDataDialog();
    }

    @Inject
    DatabaseFacade databaseFacade;
    @Inject
    UserPreferences userPreferences;
    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    @Inject
    Bus bus;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.title_confirmation)
                .setMessage(R.string.move_data_explanation)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        YandexMetrica.reportEvent(AppConstants.TRANSFER_DATA);
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                bus.post(new StartLoadEvent());
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                List<CachedVideo> cachedVideos = databaseFacade.getAllCachedVideos();
                                String outputPath = null;
                                if (userPreferences.isSdChosen()) {
                                    //sd WAS Chosen
                                    outputPath = userPreferences.getUserDownloadFolder().getPath();
                                } else {
                                    outputPath = userPreferences.getSdCardDownloadFolder().getPath();
                                }
// FIXME: 09.06.16 if sd is not available -> post event with fail

                                for (CachedVideo video : cachedVideos) {
                                    if (video != null && video.getUrl() != null && video.getStepId() >= 0) {
                                        String inputPath = (new File(video.getUrl())).getParent();
                                        StorageUtil.moveFile(inputPath, video.getVideoId() + "", outputPath);
                                        StorageUtil.moveFile(inputPath, video.getVideoId() + AppConstants.THUMBNAIL_POSTFIX_EXTENSION, outputPath);
                                        File newPathVideo = new File(outputPath, video.getVideoId() + "");
                                        File newPathThumbnail = new File(outputPath, video.getVideoId() + AppConstants.THUMBNAIL_POSTFIX_EXTENSION);

                                        String urlVideo = newPathVideo.getPath();
                                        String urlThumbnail = newPathThumbnail.getPath();
                                        video.setUrl(urlVideo);
                                        video.setThumbnail(urlThumbnail);
                                        databaseFacade.addVideo(video);
                                    }
                                }

                                if (userPreferences.isSdChosen()) {
                                    userPreferences.setSdChosen(false);
                                } else {
                                    userPreferences.setSdChosen(true);
                                }

                                return null;
                            }

                            @Override

                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                bus.post(new VideosMovedEvent());
                                bus.post(new FinishLoadEvent());
                            }


                        };
                        task.executeOnExecutor(threadPoolExecutor);
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
