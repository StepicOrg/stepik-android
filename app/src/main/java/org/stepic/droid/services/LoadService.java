package org.stepic.droid.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.WorkerThread;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepik.android.model.structure.Lesson;
import org.stepik.android.model.structure.Progress;
import org.stepik.android.model.structure.Section;
import org.stepik.android.model.structure.Step;
import org.stepik.android.model.structure.Unit;
import org.stepik.android.model.structure.Video;
import org.stepik.android.model.structure.VideoUrl;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.storage.CancelSniffer;
import org.stepic.droid.storage.StoreStateManager;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.FileUtil;
import org.stepic.droid.util.ProgressUtil;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.util.StepikLogicHelper;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.web.Api;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.ProgressesResponse;
import org.stepic.droid.web.StepResponse;
import org.stepic.droid.web.UnitMetaResponse;
import org.stepik.android.model.structure.Assignment;

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
import timber.log.Timber;

import static org.stepic.droid.storage.DownloadManagerExtensionKt.DOWNLOAD_STATUS_UNDEFINED;
import static org.stepic.droid.storage.DownloadManagerExtensionKt.getDownloadStatus;


public class LoadService extends IntentService {

    @Inject
    DownloadManager systemDownloadManager;
    @Inject
    UserPreferences userPrefs;
    @Inject
    VideoResolver resolver;
    @Inject
    Api api;
    @Inject
    DatabaseFacade databaseFacade;
    @Inject
    StoreStateManager storeStateManager;
    @Inject
    CancelSniffer cancelSniffer;
    @Inject
    Analytic analytic;

    public enum LoadTypeKey {
        Section, Lesson, Step
    }

    public LoadService() {
        super("LoadService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        App.Companion.component().inject(this);
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LoadTypeKey type = (LoadTypeKey) intent.getSerializableExtra(AppConstants.KEY_LOAD_TYPE);
        try {
            switch (type) {
                case Section:
                    Section section = intent.getParcelableExtra(AppConstants.KEY_SECTION_BUNDLE);
                    addSection(section);
                    break;
                case Lesson:
                    Lesson lesson = intent.getParcelableExtra(AppConstants.KEY_LESSON_BUNDLE);
                    addLesson(lesson);
                    break;
            }
        } catch (NullPointerException ex) {
            analytic.reportError(Analytic.Error.LOAD_SERVICE, ex);
            databaseFacade.dropDatabase();
        }
    }

    private void addDownload(String url, long fileId, String title, Step step, long sectionId) {
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
            File downloadFolderAndFile = new File(userPrefs.getUserDownloadFolder(), fileId + AppConstants.VIDEO_EXTENSION);
            if (downloadFolderAndFile.exists()) {
                //we do not need download the file, because we already have it.
                // FIXME: 20.10.15 this simple check doesn't work if file is loading and at this moment adding to Download manager Queue,
                // FIXME: 20.10.15 but this is not useless, because, work if file exists on the disk.
                // FIXME: 20.10.15 For 'singleton' file of Video (or Step) at storage use UI and Broadcasts.
                CachedVideo video = databaseFacade.getCachedVideoById(fileId);
                if (video == null) {
                    FileUtil.cleanDirectory(downloadFolderAndFile); // remove file that not present in database
                } else {
                    if (video.getStepId() != step.getId()) {
                        // trying to link video again
                        video.setStepId(step.getId());
                        databaseFacade.addVideo(video);
                    }
                    storeStateManager.updateStepAfterDeleting(step);
                    return;
                }
            }

            Uri target = Uri.fromFile(downloadFolderAndFile);

            DownloadManager.Request request =
                    new DownloadManager.Request(Uri.parse(url))
                            .setDestinationUri(target)
                            .setVisibleInDownloadsUi(false)
                            .setTitle(title + "-" + fileId)
                            .setDescription(getString(R.string.description_download));

            if (userPrefs.isNetworkMobileAllowed()) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }
            DownloadEntity downloadEntity = databaseFacade.getDownloadEntityByStepId(step.getId());

            if (downloadEntity != null && getDownloadStatus(systemDownloadManager, downloadEntity.getDownloadId()) == DOWNLOAD_STATUS_UNDEFINED) {
                databaseFacade.deleteDownloadEntityByDownloadId(downloadEntity.getDownloadId()); // sync DownloadEntities with system DownloadManager
                downloadEntity = null;
            }

            if (downloadEntity == null && !downloadFolderAndFile.exists()) {
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
                try {
                    RWLocks.SectionCancelLock.writeLock().lock();
                    if (isNeedCancel(step, sectionId)) {
                        storeStateManager.updateStepAfterDeleting(step);
                        return;
                    }
                    try {
                        // sometimes DownloadCompleteReceiver receives broadcast with this download before DownloadEntity was added
                        // RWLocks.DownloadLock.writeLock().lock trying to prevent such behavior
                        // todo improve
                        RWLocks.DownloadLock.writeLock().lock();
                        final long downloadId = systemDownloadManager.enqueue(request);
                        Timber.d("downloading %s", downloadFolderAndFile.getAbsolutePath());

                        String localThumbnail = fileId + AppConstants.THUMBNAIL_POSTFIX_EXTENSION;
                        String thumbnailsPath = FileUtil.saveFileToDisk(localThumbnail, step.getBlock().getVideo().getThumbnail(), userPrefs.getUserDownloadFolder());
                        final DownloadEntity newEntity = new DownloadEntity(downloadId, step.getId(), fileId, thumbnailsPath, videoQuality);
                        databaseFacade.addDownloadEntity(newEntity);
                    } finally {
                        RWLocks.DownloadLock.writeLock().unlock();
                    }
                } finally {
                    RWLocks.SectionCancelLock.writeLock().unlock();
                }
            }
        } catch (Exception ex) {
            storeStateManager.updateStepAfterDeleting(step);
            analytic.reportError(Analytic.Error.LOAD_SERVICE, ex);
        }

    }

    private boolean isNeedCancel(Step step, long sectionId) {
        try {
            RWLocks.CancelLock.writeLock().lock();
            if (cancelSniffer.isStepIdCanceled(step.getId())) {
                cancelSniffer.removeStepIdCancel(step.getId());
                return true;
            }
            if (sectionId > 0 && cancelSniffer.isSectionIdCanceled(sectionId)) {
                return true;
            }
        } finally {
            RWLocks.CancelLock.writeLock().unlock();
        }
        return false;
    }


    private void addStep(Step step, Lesson lesson, long sectionId) {
        if (step.getBlock().getVideo() != null) {
            Video video = step.getBlock().getVideo();
            String uri = resolver.resolveVideoUrl(video, false);
            long fileId = video.getId();
            addDownload(uri, fileId, lesson.getTitle(), step, sectionId);
        } else {
            step.setLoading(false);
            step.setCached(true);
            databaseFacade.updateOnlyCachedLoadingStep(step);
            storeStateManager.updateUnitLessonState(step.getLesson());
        }
    }

    private void addLesson(Lesson lessonOut) {
        addLesson(lessonOut, -1);
    }

    private void addLesson(Lesson lessonOut, long sectionId) {
        //if user click addLesson, it is in db already.
        //make copies of objects.
        Lesson lesson = databaseFacade.getLessonById(lessonOut.getId());

        if (lesson != null && !lesson.isCached() && lesson.isLoading()) {
            try {
                Unit unit = databaseFacade.getUnitByLessonId(lesson.getId());
                if (unit != null) {
                    List<Assignment> assignments = api.getAssignments(unit.getAssignments()).execute().body().getAssignments();
                    for (Assignment item : assignments) {
                        databaseFacade.addAssignment(item);
                    }

                    String[] ids = ProgressUtil.INSTANCE.getProgresses(assignments);
                    List<Progress> progresses = fetchProgresses(ids);
                    for (Progress item : progresses) {
                        databaseFacade.addProgress(item);
                    }
                }

                Response<StepResponse> response = api.getSteps(lesson.getSteps()).execute();
                if (response.isSuccessful()) {
                    List<Step> steps = response.body().getSteps();
                    if (steps != null && !steps.isEmpty()) {
                        for (Step step : steps) {
                            databaseFacade.addStep(step);
                            boolean cached = databaseFacade.isStepCached(step);
                            step.setCached(cached);
                        }
                        for (Step step : steps) {
                            if (!step.isCached()) {
                                step.setLoading(true);
                                step.setCached(false);
                                databaseFacade.updateOnlyCachedLoadingStep(step);
                            }
                        }

                        for (Step step : steps) {
                            if (!step.isCached()) {
                                addStep(step, lesson, sectionId);
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
            storeStateManager.updateUnitLessonState(lessonOut.getId());
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
                    Response<UnitMetaResponse> unitResponse = api.getUnits(subArrayForLoading).execute();
                    if (!unitResponse.isSuccessful()) {
                        responseIsSuccess = false;
                    } else {
                        units.addAll(unitResponse.body().getUnits());
                        pointer = lastExclusive;
                    }
                }


                if (responseIsSuccess) {
                    long[] lessonsIds = StepikLogicHelper.fromUnitsToLessonIds(units);
                    List<Progress> progresses = fetchProgresses(ProgressUtil.INSTANCE.getProgresses(units));
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

                            if (!databaseFacade.isLessonCached(lesson)) {
                                //need to be load
                                lesson.setLoading(true);
                                lesson.setCached(false);

                                databaseFacade.updateOnlyCachedLoadingLesson(lesson);
                            }
                        }
                        for (Unit unit : units) {
                            Lesson lesson = idToLessonMap.get(unit.getLesson());
                            addLesson(lesson, section.getId());
                        }
                        storeStateManager.updateSectionState(section.getId()); // FIXME DOUBLE CHECK, if all units were cached
                    } else {
                        throw new IOException("response is not success adding lessons");
                    }
                } else {
                    // if response is not success --> throw
                    throw new IOException("response is not success adding units");
                }
            } catch (UnknownHostException e) {
                //not internet
                storeStateManager.updateSectionAfterDeleting(section.getId());

            } catch (IOException e) {
                analytic.reportError(Analytic.Error.LOAD_SERVICE, e);
                storeStateManager.updateSectionAfterDeleting(section.getId());
            }
        } else if (sectionOut != null) {
            storeStateManager.updateSectionAfterDeleting(sectionOut.getId());
        }


    }

    private boolean isDownloadManagerEnabled() {
        int state;
        try {
            state = getPackageManager()
                    .getApplicationEnabledSetting("com.android.providers.downloads");
        } catch (Exception ex) {
            analytic.reportError(Analytic.Downloading.DOWNLOAD_MANAGER_IS_NOT_ENABLED, ex);
            return false;
        }

        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            analytic.reportEvent(Analytic.Downloading.DOWNLOAD_MANAGER_IS_NOT_ENABLED);
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
