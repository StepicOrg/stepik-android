package org.stepic.droid.view.fragments;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import org.stepic.droid.events.DownloadingIsLoadedSuccessfullyEvent;
import org.stepic.droid.events.loading.FinishDeletingLoadEvent;
import org.stepic.droid.events.loading.StartDeletingLoadEvent;
import org.stepic.droid.events.steps.ClearAllDownloadWithoutAnimationEvent;
import org.stepic.droid.events.steps.StepRemovedEvent;
import org.stepic.droid.events.video.DownloadReportEvent;
import org.stepic.droid.events.video.FinishDownloadCachedVideosEvent;
import org.stepic.droid.events.video.VideoCachedOnDiskEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.DownloadReportItem;
import org.stepic.droid.model.DownloadingVideoItem;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.VideosAndMapToLesson;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.view.adapters.DownloadsAdapter;
import org.stepic.droid.view.custom.LoadingProgressDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class DownloadsFragment extends FragmentBase {

    private static final int ANIMATION_DURATION = 500; //reset to 10 after debug
    private static final int UPDATE_DELAY = 300;

    public static DownloadsFragment newInstance() {
        return new DownloadsFragment();
    }

    @Bind(R.id.empty_downloading)
    View mEmptyDownloadView;

    @Bind(R.id.list_of_downloads)
    RecyclerView mDownloadsView;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;

    private DownloadsAdapter mDownloadAdapter;
    private List<CachedVideo> mCachedVideoList;
    private ConcurrentHashMap<Long, Lesson> mStepIdToLesson;
    private List<DownloadingVideoItem> mDownloadingWithProgressList;
    private Runnable mLoadingUpdater = null;
    private Set<Long> cachedStepsSet;
    private ProgressDialog loadingProgressDialog;
    private boolean isLoaded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mCachedVideoList = new ArrayList<>();
        mStepIdToLesson = new ConcurrentHashMap<>();
        mDownloadingWithProgressList = new ArrayList<>();
        cachedStepsSet = new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_downloads, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDownloadAdapter = new DownloadsAdapter(mCachedVideoList, mStepIdToLesson, getActivity(), this, mDownloadingWithProgressList, cachedStepsSet);
        mDownloadsView.setAdapter(mDownloadAdapter);

        mDownloadsView.setLayoutManager(new LinearLayoutManager(getContext()));
        mDownloadsView.setItemAnimator(new SlideInRightAnimator());
        mDownloadsView.getItemAnimator().setRemoveDuration(ANIMATION_DURATION);
        mDownloadsView.getItemAnimator().setAddDuration(ANIMATION_DURATION);
        mDownloadsView.getItemAnimator().setMoveDuration(ANIMATION_DURATION);

        if (isLoaded) {
            checkForEmpty();
        } else {
            mEmptyDownloadView.setVisibility(View.GONE);
            ProgressHelper.activate(mProgressBar);
        }

        loadingProgressDialog = new LoadingProgressDialog(getContext());
        bus.register(this);
    }

    private void startLoadingStatusUpdater() {
        if (mLoadingUpdater != null) return;
        mLoadingUpdater = new Runnable() {
            //Query the download manager about downloads that have been requested.
            @Nullable
            private Pair<Cursor, List<DownloadEntity>> getCursorAndEntitiesForAllDownloads() {
                List<DownloadEntity> nowDownloadingList = mDatabaseFacade.getAllDownloadEntities();

                long[] ids = getAllDownloadIds(nowDownloadingList);
                if (ids == null || ids.length == 0) return null;


                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(ids);
                return new Pair<>(mSystemDownloadManager.query(query), nowDownloadingList);

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
                Cursor cursor = mSystemDownloadManager.query(query);
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
                    if (pairCursorAndDownloading == null) continue;
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
                                mMainHandler.post(new Function0<Unit>() {
                                    @Override
                                    public Unit invoke() {
                                        bus.post(new DownloadingIsLoadedSuccessfullyEvent(downloadId));
                                        return Unit.INSTANCE;
                                    }
                                });
//                                Intent successLoaded = new Intent(MainApplication.getAppContext(), DownloadCompleteReceiver.class);
//                                successLoaded.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, (long) downloadId);
//                                MainApplication.getAppContext().sendBroadcast(successLoaded);
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

                                mMainHandler.post(new Function0<Unit>() {
                                    @Override
                                    public Unit invoke() {
                                        bus.post(new DownloadReportEvent(downloadingVideoItem));
                                        return Unit.INSTANCE;
                                    }
                                });
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
        mThread = new Thread(mLoadingUpdater);
        mThread.start();
    }

    @Subscribe
    public void onDownloadingSuccessfully(DownloadingIsLoadedSuccessfullyEvent event) {
        long downloadId = event.getDownloadId();
        int pos = -1;
        for (int i = 0; i < mDownloadingWithProgressList.size(); i++) {
            DownloadingVideoItem item = mDownloadingWithProgressList.get(i);
            if (item.getDownloadEntity().getDownloadId() == downloadId) {
                pos = i;
                break;
            }
        }

        if (pos >= 0 && pos <mDownloadingWithProgressList.size()){
            mDownloadingWithProgressList.remove(pos );
            mDownloadAdapter.notifyDownloadingVideoRemoved(pos);
        }

    }

    private Thread mThread;

    private void stopLoadingStatusUpdater() {
        if (mLoadingUpdater != null) {
            mThread.interrupt();
            mLoadingUpdater = null;
        }
    }

    @Subscribe
    public void onLoadingUpdate(DownloadReportEvent event) {
        DownloadingVideoItem item = event.getDownloadingVideoItem();
        int position = -1;
        for (int i = 0; i < mDownloadingWithProgressList.size(); i++) {
            if (item.getDownloadEntity().getDownloadId() == mDownloadingWithProgressList.get(i).getDownloadEntity().getDownloadId()) {
                position = i;
                break;
            }
        }
        final long stepId = item.getDownloadEntity().getStepId();
        if (!mStepIdToLesson.containsKey(stepId)) {
            mThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Step step = mDatabaseFacade.getStepById(stepId);
                    if (step != null) {

                        Lesson lesson = mDatabaseFacade.getLessonById(step.getLesson());
                        if (lesson != null) {
                            mStepIdToLesson.put(stepId, lesson);
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
            mDownloadingWithProgressList.get(position).setDownloadReportItem(item.getDownloadReportItem());
            mDownloadAdapter.notifyDownloadingVideoChanged(position); // TODO: 04.05.16 change to method update in adapter
        } else {
            mDownloadingWithProgressList.add(item);
            checkForEmpty();
            mDownloadAdapter.notifyDownloadingItemInserted(mDownloadingWithProgressList.size() - 1); // TODO: 04.05.16 change to method update in adapter
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
        mDownloadsView.setAdapter(null);
        mDownloadAdapter = null;
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
                List<CachedVideo> videos = mDatabaseFacade.getAllCachedVideos();
                List<CachedVideo> filteredVideos = new ArrayList<>();
                for (CachedVideo video : videos) {
                    if (video != null && video.getStepId() >= 0) {
                        filteredVideos.add(video);
                    }
                }
                long[] stepIds = StepicLogicHelper.fromVideosToStepIds(filteredVideos);

                Map<Long, Lesson> map = mDatabaseFacade.getMapFromStepIdToTheirLesson(stepIds);

                return new VideosAndMapToLesson(filteredVideos, map);
            }

            @Override
            protected void onPostExecute(VideosAndMapToLesson videoAndMap) {
                super.onPostExecute(videoAndMap);
                bus.post(new FinishDownloadCachedVideosEvent(videoAndMap.getCachedVideoList(), videoAndMap.getStepIdToLesson()));
            }
        };
        task.executeOnExecutor(mThreadPoolExecutor);
    }

    @Subscribe
    public void onFinishLoadCachedVideos(FinishDownloadCachedVideosEvent event) {
        showCachedVideos(event.getCachedVideos(), event.getMap());
    }

    private void showCachedVideos(List<CachedVideo> videosForShowing, Map<Long, Lesson> map) {
        isLoaded = true;
        ProgressHelper.dismiss(mProgressBar);
        if (videosForShowing == null || map == null) return;
        mStepIdToLesson.putAll(map);
        mCachedVideoList.addAll(videosForShowing);
        for (int i = 0; i < mCachedVideoList.size(); i++) {
            cachedStepsSet.add(mCachedVideoList.get(i).getStepId());
        }

        ArrayList<DownloadingVideoItem> localList = new ArrayList<>(mDownloadingWithProgressList);
        for (int i = 0; i < localList.size(); i++) {
            long stepIdOfDownloading = mDownloadingWithProgressList.get(i).getDownloadEntity().getStepId();
            if (cachedStepsSet.contains(stepIdOfDownloading)) {
                mDownloadingWithProgressList.remove(i);
                mDownloadAdapter.notifyDownloadingVideoRemoved(i);
            }
        }

        checkForEmpty();
        mDownloadAdapter.notifyDataSetChanged();
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
            mCachedVideoList.clear();
        } else {
            for (long stepId : stepIds) {
                removeByStepId(stepId);
            }
        }
        checkForEmpty();
        mDownloadAdapter.notifyDataSetChanged();
    }


    @Subscribe
    public void onStepRemoved(StepRemovedEvent e) {
        long stepId = e.getStepId();
        if (!cachedStepsSet.contains(stepId)) return;
        int position = removeByStepId(stepId);

        if (position >= 0) {
            checkForEmpty();
            mDownloadAdapter.notifyCachedVideoRemoved(position);
        }
    }

    @Subscribe
    public void onStepCached(VideoCachedOnDiskEvent event) {
        addStepToList(event.getStepId(), event.getLesson(), event.getVideo());
    }

    private void addStepToList(long stepId, Lesson lesson, CachedVideo video) {
        if (mStepIdToLesson == null || mCachedVideoList == null) return;
        mStepIdToLesson.put(stepId, lesson);
        int pos = mCachedVideoList.size();
        mCachedVideoList.add(video);
        cachedStepsSet.add(stepId);
        if (mDownloadAdapter != null && pos >= 0 && pos < mCachedVideoList.size()) {
            checkForEmpty();
            mDownloadAdapter.notifyCachedVideoInserted(stepId, pos);
        }
    }


    private int removeByStepId(long stepId) {
        if (!mStepIdToLesson.containsKey(stepId)) return -1;
        CachedVideo videoForDeleteFromList = null;
        for (CachedVideo video : mCachedVideoList) {
            if (video.getStepId() == stepId) {
                videoForDeleteFromList = video;
                break;
            }
        }

        if (videoForDeleteFromList == null) return -1;
        int position = mCachedVideoList.indexOf(videoForDeleteFromList);
        mCachedVideoList.remove(videoForDeleteFromList);
        mStepIdToLesson.remove(videoForDeleteFromList.getStepId());
        cachedStepsSet.remove(videoForDeleteFromList.getStepId());
        if (mCachedVideoList.size() == 0) {

        }
        return position;
    }

    public void checkForEmpty() {
        //// FIXME: 14.12.15 add to notify methods
        if (!mCachedVideoList.isEmpty() || !mDownloadingWithProgressList.isEmpty()) {
            ProgressHelper.dismiss(mProgressBar);
            mEmptyDownloadView.setVisibility(View.GONE);
        } else {
            mEmptyDownloadView.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onShouldStartLoad(StartDeletingLoadEvent event) {
        ProgressHelper.activate(loadingProgressDialog);
    }

    @Subscribe
    public void onShouldStopLoad(FinishDeletingLoadEvent event) {
        ProgressHelper.dismiss(loadingProgressDialog);
    }

    public void cancelAll() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                ProgressHelper.activate(loadingProgressDialog);
            }

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    RWLocks.CancelLock.writeLock().lock();
                    long[] sectionIdsLoading = mDatabaseFacade.getAllDownloadingSections();//need lock here and in loading service.
                    for (int i = 0; i < sectionIdsLoading.length; i++) {
                        Log.d("eee", "cancel section " + sectionIdsLoading[i]);
                        cancelSniffer.addSectionIdCancel(sectionIdsLoading[i]);
                        List<org.stepic.droid.model.Unit> units = mDatabaseFacade.getAllUnitsOfSection(sectionIdsLoading[i]);
                        if (!units.isEmpty()) {
                            for (org.stepic.droid.model.Unit unitItem : units) {
                                cancelSniffer.addUnitIdCancel(unitItem.getId());
                            }
                        }
                    }

                    long[] unitIdsLoading = mDatabaseFacade.getAllDownloadingUnits();
                    for (int i = 0; i < unitIdsLoading.length; i++) {
                        Log.d("eee", "cancel unit " + unitIdsLoading[i]);
                        cancelSniffer.addUnitIdCancel(unitIdsLoading[i]);

                        org.stepic.droid.model.Unit unit = mDatabaseFacade.getUnitById(unitIdsLoading[i]);
                        Lesson lesson = mDatabaseFacade.getLessonById(unit.getLesson());
                        if (lesson != null) {
                            List<Step> steps = mDatabaseFacade.getStepsOfLesson(lesson.getId());
                            if (!steps.isEmpty()) {
                                for (Step stepItem : steps) {
                                    cancelSniffer.addStepIdCancel(stepItem.getId());
                                }
                            }
                        }
                    }

                    List<DownloadEntity> downloadEntities = mDatabaseFacade.getAllDownloadEntities();
                    long stepIds[] = new long[downloadEntities.size()];
                    for (int i = 0; i < downloadEntities.size(); i++) {
                        stepIds[i] = downloadEntities.get(i).getStepId();
                    }

                    for (int i = 0; i < stepIds.length; i++) {
                        long stepId = stepIds[i];
                        cancelSniffer.addStepIdCancel(stepId);
                        mDownloadManager.cancelStep(stepId);
                    }
                } finally {
                    RWLocks.CancelLock.writeLock().unlock();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (mDownloadingWithProgressList != null && mDownloadAdapter != null) {
                    mDownloadingWithProgressList.clear();
                    mDownloadAdapter.notifyDataSetChanged();
                }
                checkForEmpty();
                ProgressHelper.dismiss(loadingProgressDialog);
            }
        };
        task.executeOnExecutor(mThreadPoolExecutor);
    }
}
