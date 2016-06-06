package org.stepic.droid.view.fragments;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.events.comments.NewCommentWasAdded;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.events.video.VideoLoadedEvent;
import org.stepic.droid.events.video.VideoResolvedEvent;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Video;
import org.stepic.droid.util.ThumbnailParser;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class VideoStepFragment extends StepBaseFragment {
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
        headerWv.setVisibility(View.GONE);

        String thumbnail = "";
        if (step.getBlock() != null && step.getBlock().getVideo() != null && step.getBlock().getVideo().getThumbnail() != null) {
            thumbnail = step.getBlock().getVideo().getThumbnail();
            setThumbnail(thumbnail);

        } else {
            Picasso.with(getContext())
                    .load(R.drawable.video_placeholder)
                    .placeholder(mVideoPlaceholder)
                    .error(mVideoPlaceholder)
                    .into(mThumbnail);
        }

        if (step.getBlock().getVideo() == null) {
            AsyncTask<Void, Void, VideoLoadedEvent> resolveTask = new AsyncTask<Void, Void, VideoLoadedEvent>() {
                @Override
                protected VideoLoadedEvent doInBackground(Void... params) {
                    //if in database not valid step (when video is loading, step has null download reference to video)
                    //try to load from web this step with many references:
                    long stepId = step.getId();
                    long stepArray[] = new long[]{stepId};
                    try {
                        List<Step> steps = mShell.getApi().getSteps(stepArray).execute().body().getSteps();
                        if (steps == null || steps.size() == 0) return null;
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
            Step localStep = step;

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
                        } else {
                            Toast.makeText(MainApplication.getAppContext(), R.string.sync_problem, Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                resolveTask.executeOnExecutor(mThreadPoolExecutor);
            }
        });
    }

    private String tempVideoUrl = null;


    @Subscribe
    public void onVideoLoaded(VideoLoadedEvent e) {
        if (e.getStepId() != step.getId()) return;

        setThumbnail(e.getThumbnail());
        tempVideoUrl = e.getVideoUrl();
    }

    private void setThumbnail(String thumbnail) {
        Uri uri = ThumbnailParser.getUriForThumbnail(thumbnail);
        Picasso.with(getContext())
                .load(uri)
                .placeholder(mVideoPlaceholder)
                .error(mVideoPlaceholder)
                .into(mThumbnail);
    }

    @Subscribe
    public void onVideoResolved(VideoResolvedEvent e) {
        if (e.getStepId() != step.getId()) return;
        mShell.getScreenProvider().showVideo(getActivity(), e.getPathToVideo());
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAdded event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }

}
