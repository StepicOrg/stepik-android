package org.stepic.droid.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

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

    PhotoViewAttacher photoViewAttacher;

    private SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
            zoomableImageView.setImageBitmap(resource);
            photoViewAttacher.update();
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

        photoViewAttacher = new PhotoViewAttacher(zoomableImageView);
        Glide.with(getContext())
                .load(url)
                .asBitmap()
                .fitCenter()
                .into(target);

    }
}
