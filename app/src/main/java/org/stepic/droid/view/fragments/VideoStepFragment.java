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

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentStepBase;
import org.stepic.droid.events.video.VideoResolvedEvent;

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
        //check for Vitamio library
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(getActivity())) {
            Log.i("Tag2", "NO Vitamio");
            return;
        } else {
            Log.i("Tag2", "Found Vitamio");
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        mVideoResolver.resolveVideoUrl(mStep.getBlock().getVideo());
    }
    @Subscribe
    public void onVideoResolved(VideoResolvedEvent e) {
        if (mStep.getBlock().getVideo().getId() != e.getVideo().getId()) return;

        Uri videoUri = Uri.parse(e.getPathToVideo());
        mVideoView.setVideoURI(videoUri);
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //// TODO: 15.10.15 implement it 
            }
        });

    }

}
