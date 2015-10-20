package org.stepic.droid.view.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentStepBase;
import org.stepic.droid.events.video.MemoryPermissionDeniedEvent;
import org.stepic.droid.events.video.VideoResolvedEvent;
import org.stepic.droid.model.Video;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class VideoStepFragment extends FragmentStepBase {
    private static final String TAG = "video_fragment";


    @Bind(R.id.test_tv)
    TextView testTv;

    @Bind(R.id.player_thumbnail)
    ImageView mThumbnail;

    @BindDrawable(R.drawable.video_placeholder)
    Drawable mVideoPlaceholder;

    @Bind(R.id.player_layout)
    View mPlayer;

    @Bind(R.id.download_btn)
    Button mDownloadButton;

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

        //// FIXME: 16.10.15 assert not null step, block, video
        Picasso.with(getContext())
                .load(mStep.getBlock().getVideo().getThumbnail())
                .placeholder(mVideoPlaceholder)
                .error(mVideoPlaceholder)
                .into(mThumbnail);

        mPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 16.10.15 change icon to loading
                String url = mVideoResolver.resolveVideoUrl(mStep.getBlock().getVideo());
                if (url != null)
                    bus.post(new VideoResolvedEvent(mStep.getBlock().getVideo(), url));
            }
        });

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Video video = mStep.getBlock().getVideo();
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Subscribe
    public void onVideoResolved(VideoResolvedEvent e) {
        if (mStep.getBlock().getVideo().getId() != e.getVideo().getId()) return;

        Uri videoUri = Uri.parse(e.getPathToVideo());
        Log.i(TAG, videoUri.getEncodedPath());

        Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
        intent.setDataAndType(videoUri, "video/*");
        //todo change icon to play
        try {
            startActivity(intent);
        } catch (Exception ex) {
            Toast.makeText(getContext(), R.string.not_video_player_error, Toast.LENGTH_LONG).show();
        }

    }

    @Subscribe
    public void onPermissionRestricted(MemoryPermissionDeniedEvent e) {
        Toast.makeText(getContext(), R.string.turn_on_permission, Toast.LENGTH_LONG).show();
    }

}
