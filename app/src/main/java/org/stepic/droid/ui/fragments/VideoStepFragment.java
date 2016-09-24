package org.stepic.droid.ui.fragments;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.core.modules.StepModule;
import org.stepic.droid.core.presenters.StepQualityPresenter;
import org.stepic.droid.core.presenters.contracts.StepQualityView;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.events.video.VideoLoadedEvent;
import org.stepic.droid.events.video.VideoResolvedEvent;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Video;
import org.stepic.droid.util.ThumbnailParser;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoStepFragment extends StepBaseFragment implements StepQualityView {
    private static final String TAG = "video_fragment";

    @BindView(R.id.player_thumbnail)
    ImageView thumbnail;

    @BindDrawable(R.drawable.video_placeholder_color)
    Drawable videoPlaceholder;

    @BindView(R.id.player_layout)
    View player;

    @Inject
    StepQualityPresenter stepQualityPresenter;

    private String tempVideoUrl = null;
    private String tempVideoQuality = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    protected void injectComponent() {
        MainApplication.component().plus(new StepModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_video_step, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //// FIXME: 16.10.15 assert not null step, block, video
        headerWvEnhanced.setVisibility(View.GONE);

        String thumbnail = "";
        if (step.getBlock() != null && step.getBlock().getVideo() != null && step.getBlock().getVideo().getThumbnail() != null) {
            thumbnail = step.getBlock().getVideo().getThumbnail();
            setThumbnail(thumbnail);
            stepQualityPresenter.determineQuality(step.getBlock().getVideo());

        } else {
            Glide.with(getContext())
                    .load("")
                    .placeholder(R.drawable.video_placeholder)
                    .into(this.thumbnail);
        }

        if (step.getBlock().getVideo() == null) {
            AsyncTask<Void, Void, VideoLoadedEvent> resolveTask = new AsyncTask<Void, Void, VideoLoadedEvent>() {
                @Override
                protected VideoLoadedEvent doInBackground(Void... params) {
                    //When video not cached (often case).
                    //if in database not valid step (when video is loading, step has null download reference to video)
                    //try to load from web this step with many references:
                    long stepId = step.getId();
                    long stepArray[] = new long[]{stepId};
                    try {
                        List<Step> steps = shell.getApi().getSteps(stepArray).execute().body().getSteps();
                        if (steps == null || steps.size() == 0) return null;
                        Step stepFromWeb = shell.getApi().getSteps(stepArray).execute().body().getSteps().get(0);
                        Video video = stepFromWeb.getBlock().getVideo();
                        if (video != null) {
                            return new VideoLoadedEvent(stepFromWeb.getBlock().getVideo(), stepFromWeb.getId(), videoResolver.resolveVideoUrl(video, step));
                        }
                        return null;
                    } catch (IOException e) {
                        analytic.reportError(Analytic.Error.CANT_RESOLVE_VIDEO, e);
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
        player.setOnClickListener(new View.OnClickListener() {
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
                            return videoResolver.resolveVideoUrl(localStep.getBlock().getVideo(), localStep);
                        }
                    }

                    @Override
                    protected void onPostExecute(String url) {
                        super.onPostExecute(url);
                        if (url != null && localStep.getBlock() != null && localStep.getBlock().getVideo() != null) {
                            bus.post(new VideoResolvedEvent(localStep.getBlock().getVideo(), url, localStep.getId()));
                        } else {
                            Toast.makeText(MainApplication.getAppContext(), R.string.sync_problem, Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                resolveTask.executeOnExecutor(threadPoolExecutor);
            }
        });
        stepQualityPresenter.attachView(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.video_step_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem qualityItemMenu = menu.findItem(R.id.action_quality);
        if (tempVideoQuality != null) {
            qualityItemMenu.setVisible(true);
            qualityItemMenu.setTitle(tempVideoQuality);
        } else {
            qualityItemMenu.setVisible(false);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroyView() {
        stepQualityPresenter.detachView(this);
        super.onDestroyView();
    }

    @Subscribe
    public void onVideoLoaded(VideoLoadedEvent e) {
        if (e.getStepId() != step.getId()) return;
        stepQualityPresenter.determineQuality(e.getVideo());
        setThumbnail(e.getVideo().getThumbnail());
        tempVideoUrl = e.getVideoUrl();
    }

    private void setThumbnail(String thumbnail) {
        Uri uri = ThumbnailParser.getUriForThumbnail(thumbnail);
        Glide.with(getContext())
                .load(uri)
                .placeholder(videoPlaceholder)
                .into(this.thumbnail);
    }

    @Subscribe
    public void onVideoResolved(VideoResolvedEvent e) {
        if (e.getStepId() != step.getId()) return;
        shell.getScreenProvider().showVideo(getActivity(), e.getPathToVideo(), e.getVideoId());
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);
    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }

    @Override
    public void showQuality(@NotNull String qualityForView) {
        updateQualityMenu(qualityForView);
    }

    private void updateQualityMenu(@NotNull String quality) {
        tempVideoQuality = quality;
        getActivity().invalidateOptionsMenu();
    }
}
