package org.stepic.droid.view.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.steps.ClearAllDownloadWithoutAnimationEvent;
import org.stepic.droid.events.steps.StepRemovedEvent;
import org.stepic.droid.events.video.FinishDownloadCachedVideosEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.VideosAndMapToLesson;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DbParseHelper;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.view.adapters.DownloadsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class DownloadsFragment extends FragmentBase {

    public static final String KEY_STRING_IDS = "step_ids";

    @Bind(R.id.empty_downloading)
    View mEmptyDownloadView;

    @Bind(R.id.list_of_downloads)
    RecyclerView mDownloadsView;

    private DownloadsAdapter mDownloadAdapter;
    private List<CachedVideo> mCachedVideoList;
    private Map<Long, Lesson> mStepIdToLesson;


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
        mCachedVideoList = new ArrayList<>();
        mStepIdToLesson = new HashMap<>();
        mDownloadAdapter = new DownloadsAdapter(mCachedVideoList, mStepIdToLesson, getContext(), this);
        mDownloadsView.setAdapter(mDownloadAdapter);

        mDownloadsView.setLayoutManager(new LinearLayoutManager(getContext()));
        mDownloadsView.setItemAnimator(new SlideInRightAnimator());
        mDownloadsView.getItemAnimator().setRemoveDuration(10);

        bus.register(this);
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
//        bus.register(this);
        updateCachedAsync();
    }

    @Override
    public void onStop() {
        super.onStop();
//        bus.unregister(this);
    }

    private void updateCachedAsync() {
        AsyncTask<Void, Void, VideosAndMapToLesson> task = new AsyncTask<Void, Void, VideosAndMapToLesson>() {
            @Override
            protected VideosAndMapToLesson doInBackground(Void... params) {
                List<CachedVideo> videos = mDatabaseManager.getAllCachedVideo();
                long[] stepIds = StepicLogicHelper.fromVideosToStepIds(videos);

                Map<Long, Lesson> map = mDatabaseManager.getMapFromStepIdToTheirLesson(stepIds);

                return new VideosAndMapToLesson(videos, map);
            }

            @Override
            protected void onPostExecute(VideosAndMapToLesson videoAndMap) {
                super.onPostExecute(videoAndMap);
                bus.post(new FinishDownloadCachedVideosEvent(videoAndMap.getCachedVideoList(), videoAndMap.getmStepIdToLesson()));
            }
        };
        task.execute();
    }

    @Subscribe
    public void onFinishLoadCachedVideos(FinishDownloadCachedVideosEvent event) {
        List<CachedVideo> list = event.getCachedVideos();
        if (list == null) {
            return;
        }

        Map<Long, Lesson> map = event.getMap();
        if (map == null) {
            return;
        }

        showCachedVideos(list, map);
    }

    private void showCachedVideos(List<CachedVideo> videosForShowing, Map<Long, Lesson> map) {
        mStepIdToLesson.clear();
        mStepIdToLesson.putAll(map);
        mCachedVideoList.clear();
        mCachedVideoList.addAll(videosForShowing);
        checkForEmpty();
        mDownloadAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.delete_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                ClearVideosDialog dialogFragment = new ClearVideosDialog();

                Bundle bundle = new Bundle();
                long[] stepIds = new long[mCachedVideoList.size()];
                int i = 0;
                for (CachedVideo videoItem : mCachedVideoList) {
                    stepIds[i++] = videoItem.getStepId();
                }
                String stringWithIds = DbParseHelper.parseLongArrayToString(stepIds);
                bundle.putString(KEY_STRING_IDS, stringWithIds);
                dialogFragment.setArguments(bundle);

                dialogFragment.show(getFragmentManager(), null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public static class ClearVideosDialog extends DialogFragment {

        @Inject
        DatabaseManager mDatabaseManager;
        @Inject
        CleanManager mCleanManager;
        @Inject
        Bus mBus;

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            MainApplication.component().inject(this);
            Bundle bundle = getArguments();
            String stringIds = bundle.getString(KEY_STRING_IDS);
            final long[] stepIds = DbParseHelper.parseStringToLongArray(stringIds);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
            builder.setTitle(R.string.title_clear_cache_dialog)
                    .setMessage(R.string.clear_videos)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            YandexMetrica.reportEvent(AppConstants.METRICA_YES_CLEAR_VIDEOS);
                            mBus.post(new ClearAllDownloadWithoutAnimationEvent(stepIds));
                            if (stepIds == null) return;
                            for (long stepId : stepIds) {
                                Step step = mDatabaseManager.getStepById(stepId);
                                mCleanManager.removeStep(step);
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, null);
            setCancelable(false);

            return builder.create();
        }
    }

    @Subscribe
    public void onClearAll(ClearAllDownloadWithoutAnimationEvent e) {
        long[] stepIds = e.getStepIds();
        if (stepIds == null) return;
        for (long stepId : stepIds) {
            removeByStepId(stepId);
        }
        checkForEmpty();
        mDownloadAdapter.notifyDataSetChanged();
    }


    @Subscribe
    public void onStepRemoved(StepRemovedEvent e) {
        long stepId = e.getStepId();

        int position = removeByStepId(stepId);

        if (position >= 0) {
            checkForEmpty();
            mDownloadAdapter.notifyItemRemoved(position);
        }
    }

    @Nullable
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
        if (mCachedVideoList.size() == 0){

        }
        return position;
    }

    public void checkForEmpty () {
        //// FIXME: 14.12.15 add to notify methods
        if (mCachedVideoList.size() != 0) {
            mEmptyDownloadView.setVisibility(View.GONE);
        } else {
            mEmptyDownloadView.setVisibility(View.VISIBLE);
        }
    }

}
