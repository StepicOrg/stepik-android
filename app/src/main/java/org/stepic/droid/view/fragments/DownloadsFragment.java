package org.stepic.droid.view.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.events.video.FinishDownloadCachedVideosEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.VideosAndMapToLesson;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.view.adapters.DownloadsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DownloadsFragment extends FragmentBase {


    @Bind(R.id.list_of_downloads)
    RecyclerView mDownloadsView;

    private DownloadsAdapter mDownloadAdapter;
    private List<CachedVideo> mCachedVideoList;
    private Map<Long, Lesson> mStepIdToLesson;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_downloads, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCachedVideoList = new ArrayList<>();
        mStepIdToLesson = new HashMap<>();
        mDownloadAdapter = new DownloadsAdapter(mCachedVideoList, mStepIdToLesson, getContext());
        mDownloadsView.setAdapter(mDownloadAdapter);

        mDownloadsView.setLayoutManager(new LinearLayoutManager(getContext()));
        mDownloadsView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
        updateCachedAsync();
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
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
        if (list == null || list.size() == 0) {
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
        mDownloadAdapter.notifyDataSetChanged();
    }
}
