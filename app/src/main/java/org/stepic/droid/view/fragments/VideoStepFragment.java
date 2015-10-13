package org.stepic.droid.view.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentStepBase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoStepFragment extends FragmentStepBase {

    @Bind(R.id.test_tv)
    TextView testTv;

    @Bind(R.id.video_view)
    VideoView mVideoView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video_step, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        testTv.setText(getArguments().getString("test"));
        Log.i("newFragment", "new");

        String url = "https://03-lvl3-pdl.vimeocdn.com/01/2389/4/111946354/307895202.mp4?expires=1444698357&token=0dabeb9e8375e26668ac7";
        Uri uri = Uri.parse(url);

        MediaController mediaController = new MediaController(getActivity());
        mVideoView.setMediaController(mediaController);
        mVideoView.setVideoURI(uri);


        mVideoView.requestFocus();
        mVideoView.start();
    }
}
