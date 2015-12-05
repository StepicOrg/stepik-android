package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
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
        mDownloadAdapter = new DownloadsAdapter(mCachedVideoList);
        mDownloadsView.setAdapter(mDownloadAdapter);

    }
}
