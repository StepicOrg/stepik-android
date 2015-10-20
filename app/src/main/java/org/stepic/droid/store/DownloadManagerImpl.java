package org.stepic.droid.store;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.squareup.otto.Bus;

import org.stepic.droid.events.video.MemoryPermissionDeniedEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.Video;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.StepResponse;
import org.stepic.droid.web.UnitStepicResponse;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

@Singleton
public class DownloadManagerImpl implements IDownloadManager {

    DownloadManager mSystemDownloadManager;
    UserPreferences mUserPrefs;
    Context mContext;
    Bus mBus;
    IVideoResolver mResolver;
    IApi mApi;

    @Inject
    public DownloadManagerImpl(Context context, UserPreferences preferences, DownloadManager dm, Bus bus, IVideoResolver resolver, IApi api) {
        mUserPrefs = preferences;
        mContext = context;
        mSystemDownloadManager = dm;
        mBus = bus;
        mResolver = resolver;
        mApi = api;
    }


    @Override
    public synchronized void addDownload(String url, String fileId) {
        if (!isDownloadManagerEnabled() || url == null)
            return;

        url = url.trim();
        if (url.length() == 0)
            return;

        try {
            Log.i("downloading", "starting download");

            File downloadFolderAndFile = new File(mUserPrefs.getDownloadFolder(), fileId);
            if (downloadFolderAndFile.exists()) {
                //we do not need download the file, because we already have it.
                return;
            }
            Uri target = Uri.fromFile(downloadFolderAndFile);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationUri(target);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);

            if (mUserPrefs.isNetworkMobileAllowed()) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }

            mSystemDownloadManager.enqueue(request);


        } catch (SecurityException ex) {
            // FIXME: 20.10.15 SHOW DIALOG WITH SUGGESTION OF PERMISSION!
            mBus.post(new MemoryPermissionDeniedEvent());
            Log.i("downloading", ex.getMessage());
        } catch (Exception ex) {
            Log.i("downloading", "downloading is failed");
        }

    }

    public synchronized void addVideoIfNeed(Video video) {
        String uri = mResolver.resolveVideoUrl(video);
        long fileId = video.getId();
        addDownload(uri, fileId + "");
    }

    @Override
    public synchronized void addSection(Section section) {
        mApi.getUnits(section.getUnits()).enqueue(new Callback<UnitStepicResponse>() {
            @Override
            public void onResponse(Response<UnitStepicResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<Unit> units = response.body().getUnits();
                    long[] lessonsIds = StepicLogicHelper.fromUnitsToLessonIds(units);
                    mApi.getLessons(lessonsIds).enqueue(new Callback<LessonStepicResponse>() {
                        @Override
                        public void onResponse(Response<LessonStepicResponse> response, Retrofit retrofit) {
                            if (response.isSuccess()) {
                                List<Lesson> lessons = response.body().getLessons();
                                for (Lesson lesson : lessons) {
                                    addLesson(lesson);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }


    @Override
    public synchronized void addLesson(Lesson lesson) {
        mApi.getSteps(lesson.getSteps()).enqueue(new Callback<StepResponse>() {
            @Override
            public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<Step> steps = response.body().getSteps();
                    for (Step step : steps) {
                        if (step.getBlock().getVideo() != null) {
                            addVideoIfNeed(step.getBlock().getVideo());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
//say something
            }
        });

    }


    @Override
    public synchronized boolean isDownloadManagerEnabled() {
        if (mContext == null) {
            return false;
        }

        int state = mContext.getPackageManager()
                .getApplicationEnabledSetting("com.android.providers.downloads");

        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            //download manager is disabled
            return false;
        }
        return true;
    }
}
