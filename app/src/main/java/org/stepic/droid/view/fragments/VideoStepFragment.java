package org.stepic.droid.view.fragments;

import android.media.MediaPlayer;
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
import org.stepic.droid.model.VideoUrl;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoStepFragment extends FragmentStepBase {

    @Bind(R.id.test_tv)
    TextView testTv;

    @Bind(R.id.video_view)
    VideoView mVideoView;
    @Bind(R.id.video_view2)
    VideoView mVideoView2;
    @Bind(R.id.video_view3)
    VideoView mVideoView3;
    @Bind(R.id.video_view4)
    VideoView mVideoView4;

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

        //check for Vitamio library
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(getActivity())) {
            Log.i("Tag2", "NO Vitamio");
            return;
        } else {
            Log.i("Tag2", "Found Vitamio");
        }


        VideoView[] videoViews = new VideoView[4];
        videoViews[0] = mVideoView;
        videoViews[1]=mVideoView2;
        videoViews[2]=mVideoView3;
        videoViews[3]=mVideoView4;


        String url = null;
        List<VideoUrl> videoUrlList = mStep.getBlock().getVideo().getUrls();
        int i = 0;
        for (VideoUrl videoUrlItem : videoUrlList) {

            Uri vidUri = Uri.parse(videoUrlItem.getUrl());
            videoViews[i].setVideoURI(vidUri);
            videoViews[i].setMediaController(new MediaController(getActivity()));
            videoViews[i].requestFocus();
            videoViews[i].setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.seekTo(mProgress);
                }
            });

//            if (videoUrlItem.getQuality().equals("720")) {
//                url = videoUrlItem.getUrl();
//                break;
//            }
            i++;
        }



//        mVideoView.start();
    }



}
