package org.stepic.droid.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.squareup.otto.Bus;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.ICancelSniffer;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.FileUtil;
import org.stepic.droid.util.ProgressUtil;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.SectionsStepicResponse;
import org.stepic.droid.web.StepResponse;
import org.stepic.droid.web.UnitStepicResponse;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
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
    DatabaseFacade mDb;
    @Inject
    IStoreStateManager mStoreStateManager;
    @Inject
    ICancelSniffer mCancelSniffer;

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
        try {
            switch (type) {
                case Course:
                    Course course = (Course) intent.getSerializableExtra(AppConstants.KEY_COURSE_BUNDLE);
                    DatabaseFacade.Table tableType = (DatabaseFacade.Table) intent.getSerializableExtra(AppConstants.KEY_TABLE_TYPE);
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
        } catch (NullPointerException ex) {
            //possibly user click clear cache;
//            throw ex;

            YandexMetrica.reportError(AppConstants.METRICA_LOAD_SERVICE, ex);
            mDb.dropDatabase();
        }
    }

    private void addDownload(String url, long fileId, String title, Step step) {
        if (!isDownloadManagerEnabled() || url == null)
            return;

        url = url.trim();
        if (url.length() == 0)
            return;

        try {

            File downloadFolderAndFile = new File(mUserPrefs.getUserDownloadFolder(), fileId + "");
            if (downloadFolderAndFile.exists()) {
                //we do not need download the file, because we already have it.
                // FIXME: 20.10.15 this simple check doesn't work if file is loading and at this moment adding to Download manager Queue,
                // FIXME: 20.10.15 but this is not useless, because, work if file exists on the disk.
                // FIXME: 20.10.15 For 'singleton' file of Video (or Step) at storage use UI and Broadcasts.
                return;
            }

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

            if (mCancelSniffer.isStepIdCanceled(step.getId())){
                mCancelSniffer.removeStepIdCancel(step.getId());
                return;
            }

            if (!mDb.isExistDownloadEntityByVideoId(fileId) && !downloadFolderAndFile.exists()) {

                String videoQuality = null;
                try {
                    for (VideoUrl urlItem : step.getBlock().getVideo().getUrls()) {
                        if (urlItem.getUrl().trim().equals(url)) {
                            videoQuality = urlItem.getQuality();
                            break;
                        }
                    }
                }
                catch (NullPointerException npe){
                    videoQuality = mUserPrefs.getQualityVideo();
                }

                    long downloadId = mSystemDownloadManager.enqueue(request);
                String local_thumbnail = fileId + AppConstants.THUMBNAIL_POSTFIX_EXTENSION;
                String thumbnailsPath = FileUtil.saveImageToDisk(local_thumbnail, step.getBlock().getVideo().getThumbnail(), mUserPrefs.getUserDownloadFolder());
                final DownloadEntity newEntity = new DownloadEntity(downloadId, step.getId(), fileId, thumbnailsPath, videoQuality);
                mDb.addDownloadEntity(newEntity);
            }
        } catch (SecurityException ex) {
            YandexMetrica.reportError(AppConstants.METRICA_LOAD_SERVICE, ex);
        } catch (Exception ex) {
            YandexMetrica.reportError(AppConstants.METRICA_LOAD_SERVICE, ex);
        }

    }

    private void addStep(Step step, Lesson lesson) {

        if (step.getBlock().getVideo() != null) {
            Video video = step.getBlock().getVideo();
            String uri = mResolver.resolveVideoUrl(video);
            long fileId = video.getId();
            addDownload(uri, fileId, lesson.getTitle(), step);
        } else {
            step.set_loading(false);
            step.set_cached(true);
            mDb.updateOnlyCachedLoadingStep(step);
            mStoreStateManager.updateUnitLessonState(step.getLesson());
        }
    }

    private void addUnitLesson(Unit unit, Lesson lesson) {
        //if user click addUnitLesson, it is in db already.
        //make copies of objects.
        unit = mDb.getUnitByLessonId(lesson.getId());
        lesson = mDb.getLessonById(lesson.getId());
        if (!unit.is_cached() && !lesson.is_cached() && unit.is_loading() && lesson.is_loading()) {

            try {
                List<Assignment> assignments = mApi.getAssignments(unit.getAssignments()).execute().body().getAssignments();
                for (Assignment item : assignments) {
                    mDb.addAssignment(item);

                }

                String[] ids = ProgressUtil.getAllProgresses(assignments);
                List<Progress> progresses = mApi.getProgresses(ids).execute().body().getProgresses();
                for (Progress item : progresses) {
                    mDb.addProgress(item);
                }
                Response<StepResponse> response = mApi.getSteps(lesson.getSteps()).execute();
                if (response.isSuccess()) {
                    List<Step> steps = response.body().getSteps();
                    if (steps != null && steps.size() != 0) {
                        for (Step step : steps) {
                            mDb.addStep(step);
                            boolean cached = mDb.isStepCached(step);
                            step.set_cached(cached);
                        }
                        for (Step step : steps) {
                            if (!step.is_cached()) {
                                step.set_loading(true);
                                step.set_cached(false);
                                mDb.updateOnlyCachedLoadingStep(step);
                            }
                        }

                        for (Step step : steps) {
                            if (!step.is_cached()) {
                                addStep(step, lesson);
                            }
                        }
                    } else {
                        mStoreStateManager.updateUnitLessonState(lesson.getId());
                    }
                } else {
                    mStoreStateManager.updateUnitLessonState(lesson.getId());
                }
            } catch (UnknownHostException e) {
                //not internet
                mStoreStateManager.updateUnitLessonAfterDeleting(lesson.getId());
            } catch (IOException e) {
                YandexMetrica.reportError(AppConstants.METRICA_LOAD_SERVICE, e);
                e.printStackTrace();
                mStoreStateManager.updateUnitLessonState(lesson.getId());
            }
        } else {
            mStoreStateManager.updateUnitLessonState(lesson.getId());
        }
    }

    private void addSection(Section section) {
        //if user click to removeSection, then section already in database.
        section = mDb.getSectionById(section.getId());//make copy of section.
        try {
            Response<UnitStepicResponse> unitLessonResponse = mApi.getUnits(section.getUnits()).execute();
            if (unitLessonResponse.isSuccess()) {
                final List<Unit> units = unitLessonResponse.body().getUnits();
                long[] lessonsIds = StepicLogicHelper.fromUnitsToLessonIds(units);
                List<Progress> progresses = mApi.getProgresses(ProgressUtil.getAllProgresses(units)).execute().body().getProgresses();
                for (Progress item : progresses) {
                    mDb.addProgress(item);
                }


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

                        if (!mDb.isUnitCached(unit) && !mDb.isLessonCached(lesson)) {
                            //need to be load

                            unit.set_loading(true);
                            unit.set_cached(false);
                            lesson.set_loading(true);
                            lesson.set_cached(false);

                            mDb.updateOnlyCachedLoadingLesson(lesson);
                            mDb.updateOnlyCachedLoadingUnit(unit);
                        }
                    }

                    for (Unit unit : units) {
                        Lesson lesson = idToLessonMap.get(unit.getLesson());
                        addUnitLesson(unit, lesson);
                    }
                }
            }
        } catch (UnknownHostException e) {
            //not internet
            mStoreStateManager.updateSectionAfterDeleting(section.getId());

        } catch (IOException e) {
            YandexMetrica.reportError(AppConstants.METRICA_LOAD_SERVICE, e);
            e.printStackTrace();
        }
    }

    @Deprecated
    private void addCourse(Course course, DatabaseFacade.Table type) {
        mDb.addCourse(course, type);
        course = mDb.getCourseById(course.getCourseId(), type); //make copy of course.

        course.set_loading(true);
        course.set_cached(false);
        mDb.updateOnlyCachedLoadingCourse(course, type);

        Response<SectionsStepicResponse> response;
        try {
            response = mApi.getSections(course.getSections()).execute();
            if (response.isSuccess()) {
                List<Section> sections = response.body().getSections();

                for (Section section : sections) {
                    mDb.addSection(section);
                    section.set_cached(false);
                    section.set_loading(true);
                    mDb.updateOnlyCachedLoadingSection(section);
                }

                for (Section section : sections) {
                    addSection(section);
                }
            }
        } catch (IOException e) {
            YandexMetrica.reportError(AppConstants.METRICA_LOAD_SERVICE, e);
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
