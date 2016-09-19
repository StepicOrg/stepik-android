package org.stepic.droid.ui.fragments;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.concurrency.DownloadPoster;
import org.stepic.droid.events.CancelAllVideosEvent;
import org.stepic.droid.events.DownloadingIsLoadedSuccessfullyEvent;
import org.stepic.droid.events.loading.FinishLoadEvent;
import org.stepic.droid.events.loading.StartLoadEvent;
import org.stepic.droid.events.steps.ClearAllDownloadWithoutAnimationEvent;
import org.stepic.droid.events.steps.StepRemovedEvent;
import org.stepic.droid.events.video.DownloadReportEvent;
import org.stepic.droid.events.video.FinishDownloadCachedVideosEvent;
import org.stepic.droid.events.video.VideoCachedOnDiskEvent;
import org.stepic.droid.events.video.VideosMovedEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.DownloadReportItem;
import org.stepic.droid.model.DownloadingVideoItem;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.VideosAndMapToLesson;
import org.stepic.droid.util.KotlinUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.ui.adapters.DownloadsAdapter;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class DownloadsFragment extends FragmentBase {

    private static final int ANIMATION_DURATION = 10; //reset to 10 after debug
    private static final int UPDATE_DELAY = 300;
    private static final int UPDATE_DELAY_WHEN_IDLE = 3000;


    public static DownloadsFragment newInstance() {
        return new DownloadsFragment();
    }

    @BindView(R.id.empty_downloading)
    View emptyDownloadView;

    @BindView(R.id.list_of_downloads)
    RecyclerView downloadsView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private DownloadsAdapter downloadAdapter;
    private List<CachedVideo> cachedVideoList;
    private ConcurrentHashMap<Long, Lesson> stepIdToLesson;
    private List<DownloadingVideoItem> downloadingWithProgressList;
    private Runnable loadingUpdater = null;
    private Set<Long> cachedStepsSet;
    private ProgressDialog loadingProgressDialog;
    private boolean isLoaded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        cachedVideoList = new ArrayList<>();
        stepIdToLesson = new ConcurrentHashMap<>();
        downloadingWithProgressList = new ArrayList<>();
        cachedStepsSet = new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_downloads, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        downloadAdapter = new DownloadsAdapter(cachedVideoList, stepIdToLesson, getActivity(), this, downloadingWithProgressList, cachedStepsSet);
        downloadsView.setAdapter(downloadAdapter);

        downloadsView.setLayoutManager(new LinearLayoutManager(getContext()));
        downloadsView.setItemAnimator(new SlideInRightAnimator());
        downloadsView.getItemAnimator().setRemoveDuration(ANIMATION_DURATION);
        downloadsView.getItemAnimator().setAddDuration(ANIMATION_DURATION);
        downloadsView.getItemAnimator().setMoveDuration(ANIMATION_DURATION);

        if (isLoaded) {
            checkForEmpty();
        } else {
            emptyDownloadView.setVisibility(View.GONE);
            ProgressHelper.activate(progressBar);
        }

        loadingProgressDialog = new LoadingProgressDialog(getContext());
        bus.register(this);
    }

    private void startLoadingStatusUpdater() {
        if (loadingUpdater != null) return;
        loadingUpdater = new Runnable() {
            //Query the download manager about downloads that have been requested.
            @Nullable
            private Pair<Cursor, List<DownloadEntity>> getCursorAndEntitiesForAllDownloads() {
                List<DownloadEntity> nowDownloadingList = databaseFacade.getAllDownloadEntities();

                long[] ids = getAllDownloadIds(nowDownloadingList);
                if (ids == null || ids.length == 0) return null;


                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(ids);
                return new Pair<>(systemDownloadManager.query(query), nowDownloadingList);

            }

            private long[] getAllDownloadIds(@NotNull List<DownloadEntity> list) {
                final List<DownloadEntity> copyOfList = new ArrayList<>(list);
                long[] result = new long[copyOfList.size()];
                int i = 0;
                for (DownloadEntity element : copyOfList) {
                    if (!cancelSniffer.isStepIdCanceled(element.getStepId()))
                        result[i++] = element.getDownloadId();
                }
                return result;
            }

            private boolean isInDownloadManager(long downloadId) {
                boolean isInDM = false;
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = systemDownloadManager.query(query);
                try {
                    isInDM = cursor.getCount() > 0;
                } finally {
                    cursor.close();
                }
                return isInDM;
            }


            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    Pair<Cursor, List<DownloadEntity>> pairCursorAndDownloading = getCursorAndEntitiesForAllDownloads();
                    if (pairCursorAndDownloading == null) {
                        try {
                            Thread.sleep(UPDATE_DELAY_WHEN_IDLE);
                        } catch (InterruptedException e) {
                            return;
                        }
                        continue;
                    }
                    Cursor cursor = pairCursorAndDownloading.first;
                    List<DownloadEntity> entities = pairCursorAndDownloading.second;
                    try {
                        cursor.moveToFirst();

                        while (!cursor.isAfterLast()) {
                            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                            int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            int columnStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            final int downloadId = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                            int columnReason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));

                            if (columnStatus == DownloadManager.STATUS_SUCCESSFUL) {
                                mainHandler.post(new Function0<Unit>() {
                                    @Override
                                    public Unit invoke() {
                                        bus.post(new DownloadingIsLoadedSuccessfullyEvent(downloadId));
                                        return Unit.INSTANCE;
                                    }
                                });
                                cursor.moveToNext();
                                continue;
                            }

                            final DownloadReportItem downloadReportItem = new DownloadReportItem(bytes_downloaded, bytes_total, columnStatus, downloadId, columnReason);
                            DownloadEntity relatedDownloadEntity = null;
                            for (DownloadEntity entity : entities) {
                                if (entity.getDownloadId() == downloadId) {
                                    relatedDownloadEntity = entity;
                                    break;
                                }
                            }

                            if (relatedDownloadEntity != null && !cancelSniffer.isStepIdCanceled(relatedDownloadEntity.getStepId()) && isInDownloadManager(relatedDownloadEntity.getDownloadId())) {
                                final DownloadingVideoItem downloadingVideoItem = new DownloadingVideoItem(downloadReportItem, relatedDownloadEntity);
                                mainHandler.post(new DownloadPoster(downloadingVideoItem));
                            }
                            cursor.moveToNext();
                        }
                    } finally {
                        cursor.close();
                    }

                    try {
                        Thread.sleep(UPDATE_DELAY); // TODO: 04.05.16 thread sleep? 2000 ms?
                    } catch (InterruptedException e) {
                        return;
                    }

                }
            }


        };
        thread = new Thread(loadingUpdater);
        thread.start();
    }

    @Subscribe
    public void onDownloadingSuccessfully(DownloadingIsLoadedSuccessfullyEvent event) {
        long downloadId = event.getDownloadId();
        int pos = -1;
        for (int i = 0; i < downloadingWithProgressList.size(); i++) {
            DownloadingVideoItem item = downloadingWithProgressList.get(i);
            if (item.getDownloadEntity().getDownloadId() == downloadId) {
                pos = i;
                break;
            }
        }

        if (pos >= 0 && pos < downloadingWithProgressList.size()) {
            downloadingWithProgressList.remove(pos);
            downloadAdapter.notifyDownloadingVideoRemoved(pos , downloadId);
        }

    }

    private Thread thread;

    private void stopLoadingStatusUpdater() {
        if (loadingUpdater != null) {
            thread.interrupt();
            loadingUpdater = null;
        }
    }

    @Subscribe
    public void onLoadingUpdate(DownloadReportEvent event) {
        DownloadingVideoItem item = event.getDownloadingVideoItem();
        int position = -1;
        for (int i = 0; i < downloadingWithProgressList.size(); i++) {
            if (item.getDownloadEntity().getDownloadId() == downloadingWithProgressList.get(i).getDownloadEntity().getDownloadId()) {
                position = i;
                break;
            }
        }
        final long stepId = item.getDownloadEntity().getStepId();
        if (!stepIdToLesson.containsKey(stepId)) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Step step = databaseFacade.getStepById(stepId);
                    if (step != null) {

                        Lesson lesson = databaseFacade.getLessonById(step.getLesson());
                        if (lesson != null) {
                            stepIdToLesson.put(stepId, lesson);
                        }
                    }
                }
            });
            return; // if we do not know about lesson name -> not show this video.
        }

        if (cachedStepsSet.contains(stepId)) {
            return; //if already cached do not show
        }

        if (position >= 0) {
            downloadingWithProgressList.get(position).setDownloadReportItem(item.getDownloadReportItem());
            downloadAdapter.notifyDownloadingVideoChanged(position, stepId);
        } else {
            downloadingWithProgressList.add(item);
            checkForEmpty();
            downloadAdapter.notifyDownloadingItemInserted(downloadingWithProgressList.size() - 1);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLoadingStatusUpdater();
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        downloadsView.setAdapter(null);
        downloadAdapter = null;
        loadingProgressDialog = null;
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoadingStatusUpdater();
        if (!isLoaded) {
            updateCachedAsync();
        }
    }

    private void updateCachedAsync() {
        AsyncTask<Void, Void, VideosAndMapToLesson> task = new AsyncTask<Void, Void, VideosAndMapToLesson>() {
            @Override
            protected VideosAndMapToLesson doInBackground(Void... params) {
                List<CachedVideo> videos = databaseFacade.getAllCachedVideos();
                List<CachedVideo> filteredVideos = new ArrayList<>();
                for (CachedVideo video : videos) {
                    if (video != null && video.getStepId() >= 0) {
                        filteredVideos.add(video);
                    }
                }
                long[] stepIds = StepicLogicHelper.fromVideosToStepIds(filteredVideos);

                Map<Long, Lesson> map = databaseFacade.getMapFromStepIdToTheirLesson(stepIds);

                return new VideosAndMapToLesson(filteredVideos, map);
            }

            @Override
            protected void onPostExecute(VideosAndMapToLesson videoAndMap) {
                super.onPostExecute(videoAndMap);
                bus.post(new FinishDownloadCachedVideosEvent(videoAndMap.getCachedVideoList(), videoAndMap.getStepIdToLesson()));
            }
        };
        task.executeOnExecutor(threadPoolExecutor);
    }

    @Subscribe
    public void onFinishLoadCachedVideos(FinishDownloadCachedVideosEvent event) {
        showCachedVideos(event.getCachedVideos(), event.getMap());
    }

    private void showCachedVideos(List<CachedVideo> videosForShowing, Map<Long, Lesson> map) {
        isLoaded = true;
        ProgressHelper.dismiss(progressBar);
        if (videosForShowing == null || map == null) return;

        stepIdToLesson.clear(); //when moved it is working
        cachedVideoList.clear();

        stepIdToLesson.putAll(map);
        cachedVideoList.addAll(videosForShowing);
        for (int i = 0; i < cachedVideoList.size(); i++) {
            cachedStepsSet.add(cachedVideoList.get(i).getStepId());
        }

        List<DownloadingVideoItem> localList = KotlinUtil.INSTANCE.filterIfNotContains(downloadingWithProgressList, cachedStepsSet);
        downloadingWithProgressList.clear();
        downloadingWithProgressList.addAll(localList);

        checkForEmpty();
        downloadAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Subscribe
    public void onClearAll(ClearAllDownloadWithoutAnimationEvent e) {
        long[] stepIds = e.getStepIds();
        if (stepIds == null) {
            cachedStepsSet.clear();
            cachedVideoList.clear();
        } else {
            for (long stepId : stepIds) {
                removeByStepId(stepId);
            }
        }
        checkForEmpty();
        downloadAdapter.notifyDataSetChanged();
    }


    @Subscribe
    public void onStepRemoved(StepRemovedEvent e) {
        long stepId = e.getStepId();
        if (!cachedStepsSet.contains(stepId)) return;
        int position = removeByStepId(stepId);

        if (position >= 0) {
            checkForEmpty();
            downloadAdapter.notifyCachedVideoRemoved(position);
        }
    }

    @Subscribe
    public void onStepCached(VideoCachedOnDiskEvent event) {
        addStepToList(event.getStepId(), event.getLesson(), event.getVideo());
    }

    private void addStepToList(long stepId, Lesson lesson, CachedVideo video) {
        if (stepIdToLesson == null || cachedVideoList == null) return;
        stepIdToLesson.put(stepId, lesson);
        int pos = cachedVideoList.size();
        cachedVideoList.add(video);
        cachedStepsSet.add(stepId);
        if (downloadAdapter != null && pos >= 0 && pos < cachedVideoList.size()) {
            checkForEmpty();
            downloadAdapter.notifyCachedVideoInserted(stepId, pos);
        }
    }


    private int removeByStepId(long stepId) {
        if (!stepIdToLesson.containsKey(stepId)) return -1;
        CachedVideo videoForDeleteFromList = null;
        for (CachedVideo video : cachedVideoList) {
            if (video.getStepId() == stepId) {
                videoForDeleteFromList = video;
                break;
            }
        }

        if (videoForDeleteFromList == null) return -1;
        int position = cachedVideoList.indexOf(videoForDeleteFromList);
        cachedVideoList.remove(videoForDeleteFromList);
        stepIdToLesson.remove(videoForDeleteFromList.getStepId());
        cachedStepsSet.remove(videoForDeleteFromList.getStepId());
        if (cachedVideoList.size() == 0) {

        }
        return position;
    }

    public void checkForEmpty() {
        //// FIXME: 14.12.15 add to notify methods
        if (!cachedVideoList.isEmpty() || !downloadingWithProgressList.isEmpty()) {
            ProgressHelper.dismiss(progressBar);
            emptyDownloadView.setVisibility(View.GONE);
        } else {
            emptyDownloadView.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onShouldStartLoad(StartLoadEvent event) {
        ProgressHelper.activate(loadingProgressDialog);
    }

    @Subscribe
    public void onShouldStopLoad(FinishLoadEvent event) {
        ProgressHelper.dismiss(loadingProgressDialog);
    }

    @Subscribe
    public void cancelAll(CancelAllVideosEvent event) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                ProgressHelper.activate(loadingProgressDialog);
            }

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    RWLocks.CancelLock.writeLock().lock();
                    long[] sectionIdsLoading = databaseFacade.getAllDownloadingSections();//need lock here and in loading service.
                    for (int i = 0; i < sectionIdsLoading.length; i++) {
                        cancelSniffer.addSectionIdCancel(sectionIdsLoading[i]);
                        List<org.stepic.droid.model.Unit> units = databaseFacade.getAllUnitsOfSection(sectionIdsLoading[i]);
                        if (!units.isEmpty()) {
                            for (org.stepic.droid.model.Unit unitItem : units) {
                                cancelSniffer.addUnitIdCancel(unitItem.getId());
                            }
                        }
                    }

                    long[] unitIdsLoading = databaseFacade.getAllDownloadingUnits();
                    for (int i = 0; i < unitIdsLoading.length; i++) {
                        cancelSniffer.addUnitIdCancel(unitIdsLoading[i]);

                        org.stepic.droid.model.Unit unit = databaseFacade.getUnitById(unitIdsLoading[i]);
                        Lesson lesson = databaseFacade.getLessonById(unit.getLesson());
                        if (lesson != null) {
                            List<Step> steps = databaseFacade.getStepsOfLesson(lesson.getId());
                            if (!steps.isEmpty()) {
                                for (Step stepItem : steps) {
                                    cancelSniffer.addStepIdCancel(stepItem.getId());
                                }
                            }
                        }
                    }

                    List<DownloadEntity> downloadEntities = databaseFacade.getAllDownloadEntities();
                    long stepIds[] = new long[downloadEntities.size()];
                    for (int i = 0; i < downloadEntities.size(); i++) {
                        stepIds[i] = downloadEntities.get(i).getStepId();
                    }

                    for (int i = 0; i < stepIds.length; i++) {
                        long stepId = stepIds[i];
                        cancelSniffer.addStepIdCancel(stepId);
                        downloadManager.cancelStep(stepId);
                    }
                } finally {
                    RWLocks.CancelLock.writeLock().unlock();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (downloadingWithProgressList != null && downloadAdapter != null) {
                    downloadingWithProgressList.clear();
                    downloadAdapter.notifyDataSetChanged();
                }
                checkForEmpty();
                ProgressHelper.dismiss(loadingProgressDialog);
            }
        };
        task.executeOnExecutor(threadPoolExecutor);
    }

    @Subscribe
    public void onVideoMoved (VideosMovedEvent event){
        updateCachedAsync();
    }
}
