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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.base.StepBaseFragment;
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
        App
                .component()
                .stepComponentBuilder()
                .build()
                .inject(this);
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

    private void setThumbnail(@Nullable String thumbnail, @Nullable final String timeString) {
        if (thumbnail != null) {
            Uri uri = ThumbnailParser.getUriForThumbnail(thumbnail);
            Glide.with(getContext())
                    .load(uri)
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            showTime(timeString);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            showTime(timeString);
                            return false;
                        }
                    })
                    .placeholder(videoPlaceholder)
                    .into(this.thumbnailImageView);
        }
    }

    private void showTime(@Nullable String timeString) {
        if (timeString != null) {
            videoLengthTextView.setVisibility(View.VISIBLE);
            videoLengthTextView.setText(timeString);
        }
    }

    @Override
    public void showQuality(@NonNull String qualityForView) {
        updateQualityMenu(qualityForView);
    }

    private void updateQualityMenu(@NonNull String quality) {
        tempVideoQuality = quality;
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onNeedOpenVideo(@NonNull String pathToVideo, long videoId) {
        player.setClickable(true);
        screenManager.showVideo(getActivity(), pathToVideo, videoId);
    }

    @Override
    public void onVideoLoaded(@Nullable String thumbnailPath, @NonNull Video video) {
        //show thumbnail and show length should be synchronized event, because we do not show thumbnail now, only after fetching length
        stepQualityPresenter.determineQuality(video);
        videoLengthPresenter.fetchLength(video, step, thumbnailPath);
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
    public void onVideoLengthDetermined(@NonNull String presentationString, @Nullable String thumbnailPath) {
        setThumbnail(thumbnailPath, presentationString);
    }

    @Override
    public void onVideoLengthFailed(@Nullable String thumbnailPath) {
        setThumbnail(thumbnailPath, null);
    }
}