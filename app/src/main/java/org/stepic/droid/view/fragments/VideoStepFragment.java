package org.stepic.droid.view.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentStepBase;
import org.stepic.droid.events.video.VideoResolvedEvent;
import org.stepic.droid.events.video.VideoLoadedEvent;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Video;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class VideoStepFragment extends FragmentStepBase {
    private static final String TAG = "video_fragment";

    @Bind(R.id.player_thumbnail)
    ImageView mThumbnail;

    @BindDrawable(R.drawable.video_placeholder)
    Drawable mVideoPlaceholder;

    @Bind(R.id.player_layout)
    View mPlayer;

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
        //// FIXME: 16.10.15 assert not null step, block, video
        mHeaderWv.setVisibility(View.GONE);

        String thumbnail = "";
        if (mStep.getBlock() != null && mStep.getBlock().getVideo() != null && mStep.getBlock().getVideo().getThumbnail() != null) {
            thumbnail = mStep.getBlock().getVideo().getThumbnail();
            setmThumbnail(thumbnail);

        } else {
            Picasso.with(getContext())
                    .load(R.drawable.video_placeholder)
                    .placeholder(mVideoPlaceholder)
                    .error(mVideoPlaceholder)
                    .into(mThumbnail);
        }

        if (mStep.getBlock().getVideo() == null) {
            AsyncTask<Void, Void, VideoLoadedEvent> resolveTask = new AsyncTask<Void, Void, VideoLoadedEvent>() {
                @Override
                protected VideoLoadedEvent doInBackground(Void... params) {
                    //if in database not valid step (when video is loading, step has null download reference to video)
                    //try to load from web this step with many references:
                    long stepId = mStep.getId();
                    long stepArray[] = new long[]{stepId};
                    try {
                        Step stepFromWeb = mShell.getApi().getSteps(stepArray).execute().body().getSteps().get(0);
                        Video video = stepFromWeb.getBlock().getVideo();
                        if (video != null) {
                            return new VideoLoadedEvent(stepFromWeb.getBlock().getVideo().getThumbnail(), stepFromWeb.getId(), mVideoResolver.resolveVideoUrl(video));
                        }
                        return null;
                    } catch (IOException e) {

                        YandexMetrica.reportError("can't Resolve video", e);
                        e.printStackTrace();
                        return null; // can't RESOLVE
                    }
                }

                @Override
                protected void onPostExecute(VideoLoadedEvent event) {
                    super.onPostExecute(event);
                    if (event != null) {
                        bus.post(event);
                    }
                }
            };
            resolveTask.execute();

        }


        mPlayer.setOnClickListener(new View.OnClickListener() {
            Step localStep = mStep;
            @Override
            public void onClick(View v) {
                // TODO: 16.10.15 change icon to loading
                AsyncTask<Void, Void, String> resolveTask = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        Video video = localStep.getBlock().getVideo();
                        if (video == null) {
                            return tempVideoUrl;
                        } else {
                            return mVideoResolver.resolveVideoUrl(localStep.getBlock().getVideo());
                        }
                    }

                    @Override
                    protected void onPostExecute(String url) {
                        super.onPostExecute(url);

                        if (url != null) {
                            bus.post(new VideoResolvedEvent(localStep.getBlock().getVideo(), url, localStep.getId()));
                            Log.i("Video", "postvideoresolved");
                        }
                    }
                };
                resolveTask.execute();
            }
        });
    }

    private String tempVideoUrl = null;

    @Subscribe
    public void onVideoLoaded(VideoLoadedEvent e) {
        if (e.getStepId() != mStep.getId()) return;

        setmThumbnail(e.getThumbnail());
        tempVideoUrl = e.getVideoUrl();
    }

    private void setmThumbnail(String thumbnail) {
        Uri uri;
        if (thumbnail.startsWith("http")) {
            uri = Uri.parse(thumbnail);
        } else {
            uri = Uri.fromFile(new File(thumbnail));
        }
        Picasso.with(getContext())
                .load(uri)
                .placeholder(mVideoPlaceholder)
                .error(mVideoPlaceholder)
                .into(mThumbnail);
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
        if (e.getStepId() != mStep.getId()) return;
        Uri videoUri = Uri.parse(e.getPathToVideo());
        Log.i(TAG, videoUri.getEncodedPath());

        Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
        intent.setDataAndType(videoUri, "video/*");
        //todo change icon to play
        try {
            startActivity(intent);
        } catch (Exception ex) {
            YandexMetrica.reportError("NotPlayer", ex);
            Toast.makeText(getContext(), R.string.not_video_player_error, Toast.LENGTH_LONG).show();
        }

    }

}
