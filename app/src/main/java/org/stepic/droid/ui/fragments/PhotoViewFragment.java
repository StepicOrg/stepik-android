package org.stepic.droid.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;

import butterknife.BindView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoViewFragment extends FragmentBase {

    private static final String pathKey = "pathKey";

    public static PhotoViewFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putString(pathKey, path);
        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.zoomable_image)
    ImageView zoomableImageView;

    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;

    @BindView(R.id.retry_button)
    View retryButton;

    @BindView(R.id.internet_problem_root)
    View internetProblemRootView;

    PhotoViewAttacher photoViewAttacher;

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


    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar();
        photoViewAttacher = new PhotoViewAttacher(zoomableImageView);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internetProblemRootView.setVisibility(View.GONE);
                loadImage();
            }
        });
        loadImage();
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
