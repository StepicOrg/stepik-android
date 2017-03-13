package org.stepic.droid.ui.fragments;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.core.modules.StepModule;
import org.stepic.droid.core.presenters.StepQualityPresenter;
import org.stepic.droid.core.presenters.VideoLengthPresenter;
import org.stepic.droid.core.presenters.VideoStepPresenter;
import org.stepic.droid.core.presenters.contracts.StepQualityView;
import org.stepic.droid.core.presenters.contracts.VideoLengthView;
import org.stepic.droid.core.presenters.contracts.VideoStepView;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Video;
import org.stepic.droid.util.ThumbnailParser;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindView;

public class VideoStepFragment extends StepBaseFragment implements StepQualityView, VideoStepView, VideoLengthView {
    @BindView(R.id.player_thumbnail)
    ImageView thumbnailImageView;

    @BindDrawable(R.drawable.video_placeholder_color)
    Drawable videoPlaceholder;

    @BindView(R.id.player_layout)
    View player;

    @BindView(R.id.videoLengthTextView)
    TextView videoLengthTextView;

    private String tempVideoQuality;

    @Inject
    VideoStepPresenter videoStepPresenter;

    @Inject
    StepQualityPresenter stepQualityPresenter;

    @Inject
    VideoLengthPresenter videoLengthPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    protected void injectComponent() {
        App.component().plus(new StepModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_step, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        headerWvEnhanced.setVisibility(View.GONE);

        stepQualityPresenter.attachView(this);

        videoStepPresenter.attachView(this);
        videoStepPresenter.initVideo(step);

        videoLengthPresenter.attachView(this);

        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.setClickable(false);
                videoStepPresenter.playVideo(step);
            }
        });
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
    public void onDestroyView() {
        videoLengthPresenter.detachView(this);
        videoStepPresenter.detachView(this);
        stepQualityPresenter.detachView(this);
        player.setOnClickListener(null);
        super.onDestroyView();
    }

    private void setThumbnail(String thumbnail) {
        Uri uri = ThumbnailParser.getUriForThumbnail(thumbnail);
        Glide.with(getContext())
                .load(uri)
                .placeholder(videoPlaceholder)
                .into(this.thumbnailImageView);
    }

    @Override
    public void showQuality(@NotNull String qualityForView) {
        updateQualityMenu(qualityForView);
    }

    private void updateQualityMenu(@NotNull String quality) {
        tempVideoQuality = quality;
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onNeedOpenVideo(@NonNull String pathToVideo, long videoId) {
        player.setClickable(true);
        shell.getScreenProvider().showVideo(getActivity(), pathToVideo, videoId);
    }

    @Override
    public void onVideoLoaded(@org.jetbrains.annotations.Nullable String thumbnailPath, @NotNull Video video) {
        if (thumbnailPath != null) {
            setThumbnail(thumbnailPath);
        }
        stepQualityPresenter.determineQuality(video);
        videoLengthPresenter.fetchLength(video, step);
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
    public void onInternetProblem() {
        player.setClickable(true);
        Toast.makeText(getContext(), R.string.sync_problem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVideoLengthDetermined(@NotNull String presentationTime) {
        videoLengthTextView.setVisibility(View.VISIBLE);
        videoLengthTextView.setText(presentationTime);
    }
}