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
//        String url = "https://player.vimeo.com/video/137358105";
//        mVideoView.setVideoURI(Uri.parse(url));
//        mVideoView.start();

        Uri uri = Uri.parse(url);

        MediaController mc = new MediaController(getActivity());
        mVideoView.setMediaController(mc);
        mVideoView.setVideoURI(uri);


        mVideoView.requestFocus();
        mVideoView.start();
    }

//MediaController media_Controller;
//    DisplayMetrics dm;
//    public void getInit(String url) {
//
//        media_Controller = new MediaController(getContext());
//        dm = new DisplayMetrics();
//        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int height = dm.heightPixels;
//        int width = dm.widthPixels;
//        mVideoView.setMinimumWidth(width);
//        mVideoView.setMinimumHeight(height);
//        mVideoView.setMediaController(media_Controller);
//        mVideoView.setVideoPath(url);
//        mVideoView.start();
//    }

}
