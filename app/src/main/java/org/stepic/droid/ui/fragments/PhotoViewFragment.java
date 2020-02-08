package org.stepic.droid.ui.fragments;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.adaptive.math.LinearRegression;
import org.stepic.droid.adaptive.ui.animations.CardAnimations;
import org.stepic.droid.base.FragmentBase;
import org.stepik.android.view.ui.layout.VerticalDragLayout;

import butterknife.BindView;
import kotlin.Unit;

public class PhotoViewFragment extends FragmentBase {

    private static final String pathKey = "pathKey";

    public static PhotoViewFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putString(pathKey, path);
        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private int screenHeight;

    @BindView(R.id.dragLayout)
    VerticalDragLayout verticalDragLayout;

    @BindView(R.id.zoomable_image)
    PhotoView zoomableImageView;

    @BindView(R.id.toolbar)
    androidx.appcompat.widget.Toolbar toolbar;

    @BindView(R.id.retry_button)
    View retryButton;

    @BindView(R.id.internet_problem_root)
    View internetProblemRootView;

    private PhotoViewAttacher photoViewAttacher;
    private int dismissPathLength;

    private Target<Bitmap> target = new CustomTarget<Bitmap>() {
        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            internetProblemRootView.setVisibility(View.GONE);
            zoomableImageView.setImageBitmap(resource);
            photoViewAttacher.update();
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {

        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            internetProblemRootView.setVisibility(View.VISIBLE);
        }
    };

    @Nullable
    String url = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        url = getArguments().getString(pathKey);
    }


    @androidx.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar();
        photoViewAttacher = new PhotoViewAttacher(zoomableImageView);
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        dismissPathLength = getResources().getDimensionPixelSize(R.dimen.dismiss_path_length);

        verticalDragLayout.setOnDragListener(dy -> {
            if (photoViewAttacher.getScale() > 1f) return Unit.INSTANCE;
            zoomableImageView.setTranslationY(-dy);
            return Unit.INSTANCE;
        });

        verticalDragLayout.setOnReleaseDragListener(dy -> {
            if (photoViewAttacher.getScale() > 1f) return Unit.INSTANCE;
            if (Math.abs(dy) > dismissPathLength) {
                CardAnimations
                        .createTransitionAnimation(zoomableImageView, 0, -Math.signum(dy) * screenHeight)
                        .rotation(0)
                        .withEndAction(() -> {
                            zoomableImageView.setVisibility(View.GONE);
                            final AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
                            if (appCompatActivity != null) {
                                appCompatActivity.finish();
                            }
                        }).start();
            } else {
                zoomableImageView.setTranslationY(0f);
            }
            return Unit.INSTANCE;
        });

        retryButton.setOnClickListener(v -> {
            internetProblemRootView.setVisibility(View.GONE);
            loadImage();
        });
        loadImage();
    }

    private float getTargetY(final float targetX) {
        final LinearRegression regression = new LinearRegression(new double[]{0, zoomableImageView.getTranslationX()}, new double[]{0, zoomableImageView.getTranslationY()});
        return (float) regression.predict(targetX);
    }

    private void loadImage() {
        Glide.with(requireContext())
                .asBitmap()
                .load(url)
                .fitCenter()
                .into(target);
    }

    private void setUpToolbar() {
        final AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
        appCompatActivity.setSupportActionBar(toolbar);
        final ActionBar supportActionBar = appCompatActivity.getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(getCloseIconDrawableRes());
        }
    }
}
