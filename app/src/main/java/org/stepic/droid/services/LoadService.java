package org.stepic.droid.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.WorkerThread;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Assignment;
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
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.util.StepikLogicHelper;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.ProgressesResponse;
import org.stepic.droid.web.StepResponse;
import org.stepic.droid.web.UnitStepicResponse;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Response;


public class LoadService extends IntentService {
    @Inject
    DownloadManager systemDownloadManager;
    @Inject
    UserPreferences userPrefs;
    @Inject
    VideoResolver resolver;
    @Inject
    IApi api;
    @Inject
    DatabaseFacade databaseFacade;
    @Inject
    IStoreStateManager storeStateManager;
    @Inject
    ICancelSniffer cancelSniffer;
    @Inject
    Analytic analytic;

    public enum LoadTypeKey {
        Section, UnitLesson, Step
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
            analytic.reportError(Analytic.Error.LOAD_SERVICE, ex);
            databaseFacade.dropDatabase();
        }
    }

    private void addDownload(String url, long fileId, String title, Step step) {
        if (!isDownloadManagerEnabled() || url == null) {
            storeStateManager.updateStepAfterDeleting(step);
            return;
        }

        url = url.trim();
        if (url.length() == 0) {
            storeStateManager.updateStepAfterDeleting(step);
            return;
        }

        try {
            File downloadFolderAndFile = new File(userPrefs.getUserDownloadFolder(), fileId + "");
            if (downloadFolderAndFile.exists()) {
                //we do not need download the file, because we already have it.
                // FIXME: 20.10.15 this simple check doesn't work if file is loading and at this moment adding to Download manager Queue,
                // FIXME: 20.10.15 but this is not useless, because, work if file exists on the disk.
                // FIXME: 20.10.15 For 'singleton' file of Video (or Step) at storage use UI and Broadcasts.
                storeStateManager.updateStepAfterDeleting(step);
                return;
            }

            Uri target = Uri.fromFile(downloadFolderAndFile);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationUri(target);
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);
            request.setTitle(title + "-" + fileId).setDescription(MainApplication.getAppContext().getString(R.string.description_download));

            if (userPrefs.isNetworkMobileAllowed()) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }
            if (isNeedCancel(step)) {
//                storeStateManager.updateStepAfterDeleting(step);
                // we check it in need cancel
                return;
            }

            if (!databaseFacade.isExistDownloadEntityByVideoId(fileId) && !downloadFolderAndFile.exists()) {

                String videoQuality = null;
                try {
                    for (VideoUrl urlItem : step.getBlock().getVideo().getUrls()) {
                        if (urlItem.getUrl().trim().equals(url)) {
                            videoQuality = urlItem.getQuality();
                            break;
                        }
                    }
                } catch (NullPointerException npe) {
                    videoQuality = userPrefs.getQualityVideo();
                }
                long downloadId = systemDownloadManager.enqueue(request);
                String local_thumbnail = fileId + AppConstants.THUMBNAIL_POSTFIX_EXTENSION;
                String thumbnailsPath = FileUtil.saveFileToDisk(local_thumbnail, step.getBlock().getVideo().getThumbnail(), userPrefs.getUserDownloadFolder());
                final DownloadEntity newEntity = new DownloadEntity(downloadId, step.getId(), fileId, thumbnailsPath, videoQuality);
                databaseFacade.addDownloadEntity(newEntity);
            }
        } catch (SecurityException ex) {
            storeStateManager.updateStepAfterDeleting(step);
            analytic.reportError(Analytic.Error.LOAD_SERVICE, ex);
        } catch (Exception ex) {
            storeStateManager.updateStepAfterDeleting(step);
            analytic.reportError(Analytic.Error.LOAD_SERVICE, ex);
        }

    }

    private boolean isNeedCancel(Step step) {
        try {
            RWLocks.CancelLock.writeLock().lock();
            if (cancelSniffer.isStepIdCanceled(step.getId())) {
                cancelSniffer.removeStepIdCancel(step.getId());
                Lesson lesson = databaseFacade.getLessonById(step.getLesson());
                if (lesson != null) {
                    Unit unit = databaseFacade.getUnitByLessonId(lesson.getId());
                    if (unit != null && cancelSniffer.isUnitIdIsCanceled(unit.getId())) {
                        storeStateManager.updateUnitLessonAfterDeleting(lesson.getId());//automatically update section
                        cancelSniffer.removeUnitIdCancel(unit.getId());

                        if (cancelSniffer.isSectionIdIsCanceled(unit.getSection())) {
                            cancelSniffer.removeSectionIdCancel(unit.getSection());
                        }
                    }

                }

                return true;
            }
        } finally {
            RWLocks.CancelLock.writeLock().unlock();
        }
        return false;
    }

    private void addStep(Step step, Lesson lesson) {

        if (step.getBlock().getVideo() != null) {
            Video video = step.getBlock().getVideo();
            String uri = resolver.resolveVideoUrl(video, step);
            long fileId = video.getId();
            addDownload(uri, fileId, lesson.getTitle(), step);
        } else {
            step.set_loading(false);
            step.set_cached(true);
            databaseFacade.updateOnlyCachedLoadingStep(step);
            storeStateManager.updateUnitLessonState(step.getLesson());
            isNeedCancel(step);
        }
    }

    private void addUnitLesson(Unit unitOut, Lesson lessonOut) {
        //if user click addUnitLesson, it is in db already.
        //make copies of objects.
        Unit unit = databaseFacade.getUnitByLessonId(lessonOut.getId());
        Lesson lesson = databaseFacade.getLessonById(lessonOut.getId());
        if (unit != null && lesson != null && !unit.is_cached() && !lesson.is_cached() && unit.is_loading() && lesson.is_loading()) {

            try {
                List<Assignment> assignments = api.getAssignments(unit.getAssignments()).execute().body().getAssignments();
                for (Assignment item : assignments) {
                    databaseFacade.addAssignment(item);

                }

                String[] ids = ProgressUtil.getAllProgresses(assignments);
                List<Progress> progresses = fetchProgresses(ids);
                for (Progress item : progresses) {
                    databaseFacade.addProgress(item);
                }
                Response<StepResponse> response = api.getSteps(lesson.getSteps()).execute();
                if (response.isSuccessful()) {
                    List<Step> steps = response.body().getSteps();
                    if (steps != null && !steps.isEmpty()) {


                        for (Step step : steps) {
                            databaseFacade.addStep(step);
                            boolean cached = databaseFacade.isStepCached(step);
                            step.set_cached(cached);
                        }
                        for (Step step : steps) {
                            if (!step.is_cached()) {
                                step.set_loading(true);
                                step.set_cached(false);
                                databaseFacade.updateOnlyCachedLoadingStep(step);
                            }
                        }
                        if (cancelSniffer.isUnitIdIsCanceled(unit.getId())) {
                            for (Step step : steps) {
                                cancelSniffer.addStepIdCancel(step.getId());
                            }
                        }

                        for (Step step : steps) {
                            if (!step.is_cached()) {
                                addStep(step, lesson);
                            }
                        }
                        storeStateManager.updateUnitLessonState(lesson.getId()); //fixme DOUBLE CHECK, IF Unit state is loading, but steps are not cached.
                    } else {
                        storeStateManager.updateUnitLessonState(lesson.getId());
                    }
                } else {
                    storeStateManager.updateUnitLessonState(lesson.getId());
                }
            } catch (UnknownHostException e) {
                //not internet
                storeStateManager.updateUnitLessonAfterDeleting(lesson.getId());
            } catch (IOException e) {
                analytic.reportError(Analytic.Error.LOAD_SERVICE, e);
                storeStateManager.updateUnitLessonAfterDeleting(lesson.getId());
            }
        } else {
            storeStateManager.updateUnitLessonAfterDeleting(lessonOut.getId());
        }
    }

    private void addSection(Section sectionOut) {
        //if user click to removeSection, then section already in database.
        Section section = databaseFacade.getSectionById(sectionOut.getId());//make copy of section.
        if (section != null) {
            try {
                boolean responseIsSuccess = true;
                final List<Unit> units = new ArrayList<>();
                long[] unitIds = section.getUnits();
                if (unitIds == null) {
                    responseIsSuccess = false;
                }
                int pointer = 0;
                while (responseIsSuccess && pointer < unitIds.length) {
                    int lastExclusive = Math.min(unitIds.length, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY);
                    long[] subArrayForLoading = Arrays.copyOfRange(unitIds, pointer, lastExclusive);
                    Response<UnitStepicResponse> unitResponse = api.getUnits(subArrayForLoading).execute();
                    if (!unitResponse.isSuccessful()) {
                        responseIsSuccess = false;
                    } else {
                        units.addAll(unitResponse.body().getUnits());
                        pointer = lastExclusive;
                    }
                }


                if (responseIsSuccess) {
                    long[] lessonsIds = StepikLogicHelper.fromUnitsToLessonIds(units);
                    List<Progress> progresses = fetchProgresses(ProgressUtil.getAllProgresses(units));
                    for (Progress item : progresses) {
                        databaseFacade.addProgress(item);
                    }


                    responseIsSuccess = true;
                    final List<Lesson> lessons = new ArrayList<>();
                    if (lessonsIds == null) {
                        responseIsSuccess = false;
                    }
                    pointer = 0;
                    while (responseIsSuccess && pointer < lessonsIds.length) {
                        int lastExclusive = Math.min(lessonsIds.length, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY);
                        long[] subArrayForLoading = Arrays.copyOfRange(lessonsIds, pointer, lastExclusive);
                        Response<LessonStepicResponse> lessonResponse = api.getLessons(subArrayForLoading).execute();
                        if (!lessonResponse.isSuccessful()) {
                            responseIsSuccess = false;
                        } else {
                            lessons.addAll(lessonResponse.body().getLessons());
                            pointer = lastExclusive;
                        }
                    }


                    if (responseIsSuccess) {
                        Map<Long, Lesson> idToLessonMap = new HashMap<>();
                        for (Lesson lesson : lessons) {
                            idToLessonMap.put(lesson.getId(), lesson);
                        }

                        for (Unit unit : units) {
                            Lesson lesson = idToLessonMap.get(unit.getLesson());


                            databaseFacade.addUnit(unit);
                            databaseFacade.addLesson(lesson);

                            if (!databaseFacade.isUnitCached(unit) && !databaseFacade.isLessonCached(lesson)) {
                                //need to be load

                                unit.set_loading(true);
                                unit.set_cached(false);
                                lesson.set_loading(true);
                                lesson.set_cached(false);

                                databaseFacade.updateOnlyCachedLoadingLesson(lesson);
                                databaseFacade.updateOnlyCachedLoadingUnit(unit);
                            }
                        }
                        if (cancelSniffer.isSectionIdIsCanceled(section.getId())) {
                            for (Unit unit : units) {
                                cancelSniffer.addUnitIdCancel(unit.getId());
                            }
                        }
                        for (Unit unit : units) {
                            Lesson lesson = idToLessonMap.get(unit.getLesson());
                            addUnitLesson(unit, lesson);
                        }
                        storeStateManager.updateSectionState(section.getId()); // FIXME DOUBLE CHECK, if all units were cached
                    } else {
                        throw new IOException("response is not success adding lessons");
                    }
                } else {
                    // if response is not succes --> throw
                    throw new IOException("response is not success adding units");
                }
            } catch (UnknownHostException e) {
                //not internet
                storeStateManager.updateSectionAfterDeleting(section.getId());

            } catch (IOException e) {
                analytic.reportError(Analytic.Error.LOAD_SERVICE, e);
                storeStateManager.updateSectionAfterDeleting(section.getId());
            }
        } else

        {
            if (sectionOut != null) {
                storeStateManager.updateSectionAfterDeleting(sectionOut.getId());
            }
        }

    }

    public boolean isDownloadManagerEnabled() {
        if (MainApplication.getAppContext() == null) {
            analytic.reportEvent(Analytic.DownloadManager.DOWNLOAD_MANAGER_IS_NOT_ENABLED);
            return false;
        }
        int state;
        try {
            state = MainApplication.getAppContext().getPackageManager()
                    .getApplicationEnabledSetting("com.android.providers.downloads");
        } catch (Exception ex) {
            analytic.reportError(Analytic.DownloadManager.DOWNLOAD_MANAGER_IS_NOT_ENABLED, ex);
            return false;
        }

        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            analytic.reportEvent(Analytic.DownloadManager.DOWNLOAD_MANAGER_IS_NOT_ENABLED);
            return false;
        }
        return true;
    }

    @WorkerThread
    private List<Progress> fetchProgresses(String[] ids) throws IOException {
        boolean responseIsSuccess = true;
        final List<Progress> progresses = new ArrayList<>();
        if (ids == null) {
            responseIsSuccess = false;
        }
        int pointer = 0;
        while (responseIsSuccess && pointer < ids.length) {
            int lastExclusive = Math.min(ids.length, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY);
            String[] subArrayForLoading = Arrays.copyOfRange(ids, pointer, lastExclusive);
            Response<ProgressesResponse> progressesResponse = api.getProgresses(subArrayForLoading).execute();
            if (!progressesResponse.isSuccessful()) {
                responseIsSuccess = false;
            } else {
                progresses.addAll(progressesResponse.body().getProgresses());
                pointer = lastExclusive;
            }
        }

        if (!responseIsSuccess) {
            throw new IOException("fail load progresses");
        }

        return progresses;
    }


}
