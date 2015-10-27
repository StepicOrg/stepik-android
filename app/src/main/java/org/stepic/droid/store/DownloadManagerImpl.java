package org.stepic.droid.store;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.squareup.otto.Bus;

import org.stepic.droid.R;
import org.stepic.droid.concurrency.ToDbCachedVideo;
import org.stepic.droid.concurrency.ToDbCoursesTask;
import org.stepic.droid.concurrency.ToDbSectionTask;
import org.stepic.droid.concurrency.ToDbStepTask;
import org.stepic.droid.concurrency.ToDbUnitLessonTask;
import org.stepic.droid.events.video.MemoryPermissionDeniedEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.Video;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.SectionsStepicResponse;
import org.stepic.droid.web.StepResponse;
import org.stepic.droid.web.UnitStepicResponse;

import java.io.File;
import java.util.HashMap;
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
    DatabaseManager mDb;

    private BroadcastReceiver mDownloadReceiver;
    private HashMap<Long, Pair<Long, Long>> mDmIdToVideoIdAndStepId;


    @Inject
    public DownloadManagerImpl(Context context, UserPreferences preferences, DownloadManager dm, Bus bus, IVideoResolver resolver, IApi api, DatabaseManager db) {
        mUserPrefs = preferences;
        mContext = context;
        mSystemDownloadManager = dm;
        mBus = bus;
        mResolver = resolver;
        mApi = api;
        mDb = db;


        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mDmIdToVideoIdAndStepId = new HashMap<>();
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (mDmIdToVideoIdAndStepId.keySet().contains(referenceId)) {
                    long video_id = mDmIdToVideoIdAndStepId.get(referenceId).first;
                    long step_id = mDmIdToVideoIdAndStepId.get(referenceId).second;
                    mDmIdToVideoIdAndStepId.remove(referenceId);
                    File downloadFolderAndFile = new File(mUserPrefs.getDownloadFolder(), video_id + "");
                    String path = Uri.fromFile(downloadFolderAndFile).getPath();
                    CachedVideo cachedVideo = new CachedVideo(step_id, video_id, path, null);

                    ToDbCachedVideo saveVideoToDb = new ToDbCachedVideo(cachedVideo);
                    saveVideoToDb.execute();
                }
            }
        };
        context.registerReceiver(mDownloadReceiver, filter);
    }


    private synchronized void addDownload(String url, long fileId, String title, Step step) {
        if (!isDownloadManagerEnabled() || url == null)
            return;

        url = url.trim();
        if (url.length() == 0)
            return;

        try {

            File downloadFolderAndFile = new File(mUserPrefs.getDownloadFolder(), fileId + "");
            if (downloadFolderAndFile.exists()) {
                //we do not need download the file, because we already have it.
                // FIXME: 20.10.15 this simple check doesn't work if file is loading and at this moment adding to Download manager Queue, 
                // FIXME: 20.10.15 but this is not useless, because, work if file exists on the disk.
                // FIXME: 20.10.15 For 'singleton' file of Video (or Step) at storage use UI and Broadcasts.
                return;
            }

            Log.i("downloading", downloadFolderAndFile.toString());
            Uri target = Uri.fromFile(downloadFolderAndFile);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationUri(target);
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);
            request.setTitle(title + "-" + fileId).setDescription(mContext.getString(R.string.description_download));

            if (mUserPrefs.isNetworkMobileAllowed()) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }

            long downloadId = mSystemDownloadManager.enqueue(request);
            mDmIdToVideoIdAndStepId.put(downloadId, new Pair(fileId, step.getId()));


        } catch (SecurityException ex) {
            // FIXME: 20.10.15 SHOW DIALOG WITH SUGGESTION OF PERMISSION!
            mBus.post(new MemoryPermissionDeniedEvent());
            Log.i("downloading", ex.getMessage());
        } catch (Exception ex) {
            Log.i("downloading", "downloading is failed");
        }

    }

    public synchronized void addStep(Step step, String title) {
        ToDbStepTask saveStep = new ToDbStepTask(step);
        saveStep.execute();

        if (step.getBlock().getVideo() != null) {
            Video video = step.getBlock().getVideo();
            if (video == null) return;
            String uri = mResolver.resolveVideoUrl(video);
            long fileId = video.getId();
            addDownload(uri, fileId, title, step);
        }
    }

    @Override
    public synchronized void addSection(Section section) {
        ToDbSectionTask sectionTask = new ToDbSectionTask(section);
        sectionTask.execute();


        mApi.getUnits(section.getUnits()).enqueue(new Callback<UnitStepicResponse>() {
            @Override
            public void onResponse(Response<UnitStepicResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    final List<Unit> units = response.body().getUnits();
                    long[] lessonsIds = StepicLogicHelper.fromUnitsToLessonIds(units);
                    mApi.getLessons(lessonsIds).enqueue(new Callback<LessonStepicResponse>() {
                        @Override
                        public void onResponse(Response<LessonStepicResponse> response, Retrofit retrofit) {
                            if (response.isSuccess()) {
                                List<Lesson> lessons = response.body().getLessons();
                                int i = 0;
                                for (Lesson lesson : lessons) {
                                    Unit unit = units.get(i++);
                                    addUnitLesson(unit, lesson);
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
    public void addCourse(final Course course, DatabaseManager.Table type) {
        ToDbCoursesTask saveTask = new ToDbCoursesTask(course, type);
        saveTask.execute();
        mApi.getSections(course.getSections()).enqueue(new Callback<SectionsStepicResponse>() {
            @Override
            public void onResponse(Response<SectionsStepicResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<Section> sections = response.body().getSections();
                    for (Section section : sections) {
                        addSection(section);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }


    @Override
    public synchronized void addUnitLesson(final Unit unit, final Lesson lesson) {
        ToDbUnitLessonTask saveTask = new ToDbUnitLessonTask(unit, lesson);
        saveTask.execute();

        mApi.getSteps(lesson.getSteps()).enqueue(new Callback<StepResponse>() {
            @Override
            public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<Step> steps = response.body().getSteps();
                    for (Step step : steps) {
                        final Step localStep = step;
                        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                addStep(localStep, lesson.getTitle());
                                return null;
                            }
                        };
                        asyncTask.execute();
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
            return false;
        }
        return true;
    }
}
