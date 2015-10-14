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

public class VideoStepFragment extends FragmentStepBase implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

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

        //check for Vitamio library
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(getActivity())) {
            Log.i("Tag2", "NO Vitamio");
            return;
        } else {
            Log.i("Tag2", "Found Vitamio");
        }


        String url = null;
        List<VideoUrl> videoUrlList = mStep.getBlock().getVideo().getUrls();
        for (VideoUrl videoUrlItem : videoUrlList) {
            if (videoUrlItem.getQuality().equals("270")) {
                url = videoUrlItem.getUrl();
                break;
            }
        }
        Uri vidUri = Uri.parse(url);

        mVideoView.setVideoURI(vidUri);
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.seekTo(mProgress);
            }
        });

//        mVideoView.start();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case io.vov.vitamio.MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                }
                break;
            case io.vov.vitamio.MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                break;
            case io.vov.vitamio.MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
//                            mDownloadRateView.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }


}
