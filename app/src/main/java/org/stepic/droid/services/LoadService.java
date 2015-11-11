package org.stepic.droid.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.squareup.otto.Bus;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.Video;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.SectionsStepicResponse;
import org.stepic.droid.web.StepResponse;
import org.stepic.droid.web.UnitStepicResponse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import retrofit.Response;


public class LoadService extends IntentService {
    @Inject
    DownloadManager mSystemDownloadManager;
    @Inject
    UserPreferences mUserPrefs;
    @Inject
    Bus mBus;
    @Inject
    IVideoResolver mResolver;
    @Inject
    IApi mApi;
    @Inject
    DatabaseManager mDb;
    @Inject
    IStoreStateManager mStoreStateManager;

    public enum LoadTypeKey {
        Course, Section, UnitLesson, Step
    }


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p/>
     * name Used to name the worker thread, important only for debugging.
     */
    public LoadService() {
        super("Loading_video_service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainApplication.component().inject(this);
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LoadTypeKey type = (LoadTypeKey) intent.getSerializableExtra(AppConstants.KEY_LOAD_TYPE);
        Log.i("downloading", "Service: " + Thread.currentThread().getName());
        switch (type) {
            case Course:
                Course course = (Course) intent.getSerializableExtra(AppConstants.KEY_COURSE_BUNDLE);
                DatabaseManager.Table tableType = (DatabaseManager.Table) intent.getSerializableExtra(AppConstants.KEY_TABLE_TYPE);
                addCourse(course, tableType);
                break;
            case Section:
                Section section = (Section) intent.getSerializableExtra(AppConstants.KEY_SECTION_BUNDLE);
                addSection(section);
                break;
            case UnitLesson:
                Unit unit = (Unit) intent.getSerializableExtra(AppConstants.KEY_UNIT_BUNDLE);
                Lesson lesson = (Lesson) intent.getSerializableExtra(AppConstants.KEY_LESSON_BUNDLE);
                addUnitLesson(unit, lesson);
                break;
            case Step:
                Step step = (Step) intent.getSerializableExtra(AppConstants.KEY_STEP_BUNDLE);
                Lesson lessonForStep = (Lesson) intent.getSerializableExtra(AppConstants.KEY_LESSON_BUNDLE);
                addStep(step, lessonForStep);
                break;
        }
    }

    private void addDownload(String url, long fileId, String title, Step step) {
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
            request.setTitle(title + "-" + fileId).setDescription(MainApplication.getAppContext().getString(R.string.description_download));

            if (mUserPrefs.isNetworkMobileAllowed()) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }

            if (!mDb.isExistDownloadEntityByVideoId(fileId) && !downloadFolderAndFile.exists()) {
                long downloadId = mSystemDownloadManager.enqueue(request);
                final DownloadEntity newEntity = new DownloadEntity(downloadId, step.getId(), fileId, step.getBlock().getVideo().getThumbnail());
                mDb.addDownloadEntity(newEntity);

            }
        } catch (SecurityException ex) {
            // FIXME: 20.10.15 SHOW DIALOG WITH SUGGESTION OF PERMISSION!
//            mBus.post(new MemoryPermissionDeniedEvent());
            //it is not main thread
            Log.i("downloading", ex.getMessage());
        } catch (Exception ex) {
            Log.i("downloading", "downloading is failed");
        }

    }

    public void addStep(Step step, Lesson lesson) {
        mDb.addStep(step);

        if (step.getBlock().getVideo() != null) {

            Video video = step.getBlock().getVideo();
            String uri = mResolver.resolveVideoUrl(video);
            long fileId = video.getId();
            addDownload(uri, fileId, lesson.getTitle(), step);
        } else {
            step.setIs_loading(false);
            step.setIs_cached(true);
            mDb.updateOnlyCachedLoadingStep(step);
            mStoreStateManager.updateUnitLessonState(step.getLesson());
        }
    }

    public void addUnitLesson(final Unit unit, final Lesson lesson) {


        try {
            Response<StepResponse> response = mApi.getSteps(lesson.getSteps()).execute();
            if (response.isSuccess()) {
                List<Step> steps = response.body().getSteps();
                if (steps != null && steps.size() != 0) {

                    for (Step step : steps) {
                        step.setIs_loading(true);
                        step.setIs_cached(false);
                        mDb.updateOnlyCachedLoadingStep(step);
                    }

                    for (Step step : steps) {
                        addStep(step, lesson);
                    }
                } else {
                    mStoreStateManager.updateUnitLessonState(lesson.getId());
                }
            } else {
                mStoreStateManager.updateUnitLessonState(lesson.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
            mStoreStateManager.updateUnitLessonState(lesson.getId());
        }
    }

    public void addSection(Section section) {

        try {
            Response<UnitStepicResponse> unitLessonResponse = mApi.getUnits(section.getUnits()).execute();
            if (unitLessonResponse.isSuccess()) {
                final List<Unit> units = unitLessonResponse.body().getUnits();
                long[] lessonsIds = StepicLogicHelper.fromUnitsToLessonIds(units);
                Response<LessonStepicResponse> response = mApi.getLessons(lessonsIds).execute();
                if (response.isSuccess()) {
                    List<Lesson> lessons = response.body().getLessons();
                    Map<Long, Lesson> idToLessonMap = new HashMap<>();
                    for (Lesson lesson : lessons) {
                        idToLessonMap.put(lesson.getId(), lesson);
                    }

                    for (Unit unit : units) {
                        Lesson lesson = idToLessonMap.get(unit.getLesson());


                        mDb.addUnit(unit);
                        mDb.addLesson(lesson);

                        unit.setIs_loading(true);
                        unit.setIs_cached(false);
                        lesson.setIs_loading(true);
                        lesson.setIs_cached(false);

                        mDb.updateOnlyCachedLoadingLesson(lesson);
                        mDb.updateOnlyCachedLoadingUnit(unit);
                    }

                    for (Unit unit : units) {
                        Lesson lesson = idToLessonMap.get(unit.getLesson());
                        addUnitLesson(unit, lesson);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCourse(final Course course, DatabaseManager.Table type) {
        mDb.addCourse(course, type);

        course.setIs_loading(true);
        course.setIs_cached(false);
        mDb.updateOnlyCachedLoadingCourse(course, type);

        Response<SectionsStepicResponse> response = null;
        try {
            response = mApi.getSections(course.getSections()).execute();
            if (response.isSuccess()) {
                List<Section> sections = response.body().getSections();

                for (Section section : sections) {
                    mDb.addSection(section);
                    section.setIs_cached(false);
                    section.setIs_loading(true);
                    mDb.updateOnlyCachedLoadingSection(section);
                }

                for (Section section : sections) {
                    addSection(section);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean isDownloadManagerEnabled() {
        if (MainApplication.getAppContext() == null) {
            return false;
        }

        int state = MainApplication.getAppContext().getPackageManager()
                .getApplicationEnabledSetting("com.android.providers.downloads");

        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            return false;
        }
        return true;
    }


}
