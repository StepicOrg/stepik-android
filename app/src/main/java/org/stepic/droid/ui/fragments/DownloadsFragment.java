package org.stepic.droid.ui.fragments;

import android.app.DownloadManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.base.Client;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.downloads.contract.DownloadsListener;
import org.stepic.droid.core.downloads.contract.DownloadsPoster;
import org.stepic.droid.core.videomoves.contract.VideosMovedListener;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.DownloadReportItem;
import org.stepic.droid.model.DownloadingVideoItem;
import org.stepik.android.model.structure.Lesson;
import org.stepik.android.model.structure.Step;
import org.stepic.droid.model.VideosAndMapToLesson;
import org.stepic.droid.ui.adapters.DownloadsAdapter;
import org.stepic.droid.ui.dialogs.CancelVideosDialog;
import org.stepic.droid.ui.dialogs.ClearVideosDialog;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.ui.listeners.OnClickCancelListener;
import org.stepic.droid.ui.util.ToolbarHelperKt;
import org.stepic.droid.util.DbParseHelper;
import org.stepic.droid.util.KotlinUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.util.StepikLogicHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

//// TODO: 26.12.16 rewrite this class to MVP
public class DownloadsFragment extends FragmentBase implements
        OnClickCancelListener,
        ClearVideosDialog.Callback,
        CancelVideosDialog.Callback,
        VideosMovedListener,
        DownloadsListener {

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

    @BindView(R.id.needAuthView)
    View needAuthRootView;

    @BindView(R.id.authAction)
    Button authUserButton;

    @BindView(R.id.goToCatalog)
    Button goToCatalog;

    private DownloadsAdapter downloadAdapter;
    private List<CachedVideo> cachedVideoList;
    private ConcurrentHashMap<Long, Lesson> stepIdToLesson;
    private List<DownloadingVideoItem> downloadingWithProgressList;
    private Runnable loadingUpdater = null;
    private Set<Long> cachedStepsSet;
    private LoadingProgressDialog loadingProgressDialog;
    private boolean isLoaded;

    @Inject
    Client<VideosMovedListener> videoMovedListenerClient;

    @Inject
    Client<DownloadsListener> downloadsListenerClient;

    @Inject
    DownloadsPoster downloadsPoster;

    @Override
    protected void injectComponent() {
        App
                .Companion
                .componentManager()
                .downloadsComponent()
                .inject(this);
    }

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
        return inflater.inflate(R.layout.fragment_downloads, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nullifyActivityBackground();
        setHasOptionsMenu(true);
        initToolbar();

        needAuthRootView.setVisibility(View.GONE);
        authUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreenManager().showLaunchScreen(getActivity());
            }
        });
        downloadAdapter = new DownloadsAdapter(cachedVideoList, stepIdToLesson, getActivity(), this, downloadingWithProgressList, cachedStepsSet, this);
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

        goToCatalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenManager.showCatalog(getContext());
            }
        });

        loadingProgressDialog = new LoadingProgressDialog(getContext());
        downloadsListenerClient.subscribe(this);
        videoMovedListenerClient.subscribe(this);
    }

    private void initToolbar() {
        ToolbarHelperKt.initCenteredToolbar(this, R.string.downloads, true);
    }

    private void startLoadingStatusUpdater() {
        if (loadingUpdater != null) return;
        loadingUpdater = new Runnable() {
            WeakReference<DownloadsFragment> downloadsFragmentWeakReference = new WeakReference<>(DownloadsFragment.this);

            //Query the download manager about downloads that have been requested.
            @Nullable
            private Pair<Cursor, List<DownloadEntity>> getCursorAndEntitiesForAllDownloads() {
                List<DownloadEntity> nowDownloadingList = getDatabaseFacade().getAllDownloadEntities();

                long[] ids = getAllDownloadIds(nowDownloadingList);
                if (ids == null || ids.length == 0) return null;


                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(ids);
                return new Pair<>(getSystemDownloadManager().query(query), nowDownloadingList);

            }

            private long[] getAllDownloadIds(@NotNull List<DownloadEntity> list) {
                final List<DownloadEntity> copyOfList = new ArrayList<>(list);
                long[] result = new long[copyOfList.size()];
                int i = 0;
                for (DownloadEntity element : copyOfList) {
                    if (!getCancelSniffer().isStepIdCanceled(element.getStepId()))
                        result[i++] = element.getDownloadId();
                }
                return result;
            }

            private boolean isInDownloadManager(long downloadId) {
                boolean isInDM = false;
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = getSystemDownloadManager().query(query);
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
                                getMainHandler().post(new Function0<kotlin.Unit>() {
                                    @Override
                                    public kotlin.Unit invoke() {
                                        DownloadsFragment downloadsFragment = downloadsFragmentWeakReference.get();
                                        if (downloadsFragment != null) {
                                            downloadsFragment.removeVideoFromDownloading(downloadId);
                                        }
                                        return kotlin.Unit.INSTANCE;
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

                            if (relatedDownloadEntity != null && !getCancelSniffer().isStepIdCanceled(relatedDownloadEntity.getStepId()) && isInDownloadManager(relatedDownloadEntity.getDownloadId())) {
                                final DownloadingVideoItem downloadingVideoItem = new DownloadingVideoItem(downloadReportItem, relatedDownloadEntity);
                                getMainHandler().post(new Function0<Unit>() {
                                    @Override
                                    public Unit invoke() {
                                        downloadsPoster.downloadUpdate(downloadingVideoItem);
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
        thread = new Thread(loadingUpdater);
        thread.start();
    }

    public void removeVideoFromDownloading(long downloadId) {
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
            downloadAdapter.notifyDownloadingVideoRemoved(pos, downloadId);
        }

    }

    private Thread thread;

    private void stopLoadingStatusUpdater() {
        if (loadingUpdater != null) {
            thread.interrupt();
            loadingUpdater = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLoadingStatusUpdater();
    }

    @Override
    public void onDestroyView() {
        downloadsListenerClient.unsubscribe(this);
        videoMovedListenerClient.unsubscribe(this);
        authUserButton.setOnClickListener(null);
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
                List<CachedVideo> videos = getDatabaseFacade().getAllCachedVideos();
                List<CachedVideo> filteredVideos = new ArrayList<>();
                for (CachedVideo video : videos) {
                    if (video != null && video.getStepId() >= 0) {
                        filteredVideos.add(video);
                    }
                }
                long[] stepIds = StepikLogicHelper.fromVideosToStepIds(filteredVideos);

                Map<Long, Lesson> map = getDatabaseFacade().getMapFromStepIdToTheirLesson(stepIds);

                return new VideosAndMapToLesson(filteredVideos, map);
            }

            @Override
            protected void onPostExecute(VideosAndMapToLesson videoAndMap) {
                super.onPostExecute(videoAndMap);
                downloadsPoster.finishDownloadVideo(videoAndMap.getCachedVideoList(), videoAndMap.getStepIdToLesson());
            }
        };
        task.executeOnExecutor(getThreadPoolExecutor());
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
            case android.R.id.home:
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        return position;
    }

    public void checkForEmpty() {
        //// FIXME: 14.12.15 add to notify methods
        if (getSharedPreferenceHelper().getAuthResponseFromStore() == null) {
            emptyDownloadView.setVisibility(View.GONE);
            downloadsView.setVisibility(View.GONE);
            needAuthRootView.setVisibility(View.VISIBLE);
        } else {
            needAuthRootView.setVisibility(View.GONE);
            if (!cachedVideoList.isEmpty() || !downloadingWithProgressList.isEmpty()) {
                ProgressHelper.dismiss(progressBar);
                emptyDownloadView.setVisibility(View.GONE);
            } else {
                emptyDownloadView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCancelAllVideos() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                ProgressHelper.activate(loadingProgressDialog);
            }

            @Override
            protected Void doInBackground(Void[] params) {
                try {
                    RWLocks.CancelLock.writeLock().lock();

                    long[] lessonIds = getDatabaseFacade().getAllDownloadingLessons();
                    for (long lessonId : lessonIds) {
                        Lesson lesson = getDatabaseFacade().getLessonById(lessonId);
                        if (lesson != null) {
                            List<Step> steps = getDatabaseFacade().getStepsOfLesson(lesson.getId());
                            if (!steps.isEmpty()) {
                                for (Step stepItem : steps) {
                                    getCancelSniffer().addStepIdCancel(stepItem.getId());
                                }
                            }
                        }
                    }

                    List<DownloadEntity> downloadEntities = getDatabaseFacade().getAllDownloadEntities();
                    long stepIds[] = new long[downloadEntities.size()];
                    for (int i = 0; i < downloadEntities.size(); i++) {
                        stepIds[i] = downloadEntities.get(i).getStepId();
                    }

                    for (long stepId : stepIds) {
                        getCancelSniffer().addStepIdCancel(stepId);
                        getDownloadManager().cancelStep(stepId);
                    }
                } finally {
                    RWLocks.CancelLock.writeLock().unlock();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                if (downloadingWithProgressList != null && downloadAdapter != null) {
                    downloadingWithProgressList.clear();
                    downloadAdapter.notifyDataSetChanged();
                }
                checkForEmpty();
                ProgressHelper.dismiss(loadingProgressDialog);
            }
        };
        task.executeOnExecutor(getThreadPoolExecutor());
    }

    @Override
    public void onClickCancel(int position) {
        if (position == 0 && !downloadingWithProgressList.isEmpty()) {
            //downloading

            DialogFragment dialogFragment = CancelVideosDialog.Companion.newInstance();
            dialogFragment.setTargetFragment(this, 0);
            if (!dialogFragment.isAdded()) {
                dialogFragment.show(getFragmentManager(), null);
            }
        } else {
            //cached
            ClearVideosDialog dialogFragment = ClearVideosDialog.Companion.newInstance();
            dialogFragment.setTargetFragment(this, 0);

            Bundle bundle = new Bundle();
            long[] stepIds = new long[cachedVideoList.size()];
            int i = 0;
            for (CachedVideo videoItem : cachedVideoList) {
                stepIds[i++] = videoItem.getStepId();
            }
            String stringWithIds = DbParseHelper.parseLongArrayToString(stepIds);
            bundle.putString(ClearVideosDialog.Companion.getKEY_STRING_IDS(), stringWithIds);
            dialogFragment.setArguments(bundle);

            if (!dialogFragment.isAdded()) {
                dialogFragment.show(getFragmentManager(), null);
            }
        }
    }

    @Override
    public void onStartLoading() {
        ProgressHelper.activate(loadingProgressDialog);
    }

    @Override
    public void onFinishLoading() {
        ProgressHelper.dismiss(loadingProgressDialog);
    }

    @Override
    public void onClearAllWithoutAnimation(@Nullable long[] stepIds) {
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

    @Override
    public void onVideosMoved() {
        updateCachedAsync();
    }

    @Override
    public void onDownloadComplete(long stepId, @NotNull Lesson lesson, @NotNull CachedVideo video) {
        addStepToList(stepId, lesson, video);
    }

    @Override
    public void onDownloadFailed(long downloadId) {
        removeVideoFromDownloading(downloadId);
    }

    @Override
    public void onDownloadUpdate(@NotNull DownloadingVideoItem item) {
        int position = -1;
        for (int i = 0; i < downloadingWithProgressList.size(); i++) {
            if (item.getDownloadEntity().getDownloadId() == downloadingWithProgressList.get(i).getDownloadEntity().getDownloadId()) {
                position = i;
                break;
            }
        }
        final long stepId = item.getDownloadEntity().getStepId();
        if (!stepIdToLesson.containsKey(stepId)) {
            getThreadPoolExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Step step = getDatabaseFacade().getStepById(stepId);
                    if (step != null) {

                        Lesson lesson = getDatabaseFacade().getLessonById(step.getLesson());
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
    public void onFinishDownloadVideo(@NotNull List<CachedVideo> list, @NotNull Map<Long, Lesson> map) {
        showCachedVideos(list, map);
    }

    @Override
    public void onStepRemoved(long stepId) {
        if (!cachedStepsSet.contains(stepId)) return;
        int position = removeByStepId(stepId);

        if (position >= 0) {
            checkForEmpty();
            downloadAdapter.notifyCachedVideoRemoved(position);
        }
    }
}
