package org.stepic.droid.view.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.stepic.droid.view.adapters.DownloadsAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DownloadsFragment extends FragmentBase {


    @Bind(R.id.list_of_downloads)
    RecyclerView mDownloadsView;

    private DownloadsAdapter mDownloadAdapter;
    private List<CachedVideo> mCachedVideoList;


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
        mDownloadAdapter = new DownloadsAdapter(mCachedVideoList, getContext());
        mDownloadsView.setAdapter(mDownloadAdapter);

        mDownloadsView.setLayoutManager(new LinearLayoutManager(getContext()));

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
        AsyncTask<Void, Void, List<CachedVideo>> task = new AsyncTask<Void, Void, List<CachedVideo>>() {
            @Override
            protected List<CachedVideo> doInBackground(Void... params) {
                return mDatabaseManager.getAllCachedVideo();
            }

            @Override
            protected void onPostExecute(List<CachedVideo> cachedVideos) {
                super.onPostExecute(cachedVideos);
                bus.post(new FinishDownloadCachedVideosEvent(cachedVideos));
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
        showCachedVideos(list);

    }

    private void showCachedVideos(List<CachedVideo> videosForShowing) {
        mCachedVideoList.clear();
        mCachedVideoList.addAll(videosForShowing);
        mDownloadAdapter.notifyDataSetChanged();
    }
}
