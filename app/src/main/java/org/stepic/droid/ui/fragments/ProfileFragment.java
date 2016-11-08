package org.stepic.droid.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.ProfilePresenter;
import org.stepic.droid.core.modules.ProfileModule;
import org.stepic.droid.core.presenters.contracts.ProfileView;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;

public class ProfileFragment extends FragmentBase implements ProfileView {

    public static ProfileFragment newInstance() {

        Bundle args = new Bundle();

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.profileName)
    TextView profileName;

    @BindView(R.id.profileImage)
    ImageView profileImage;

    @BindDrawable(R.drawable.placeholder_icon)
    Drawable userPlaceholder;

    @BindView(R.id.shortBioValue)
    TextView shortBioValue;

    @BindView(R.id.shortBioTitle)
    TextView shortBioTitle;

    @BindString(R.string.about_me)
    String aboutMeTitle;

    @BindString(R.string.short_info)
    String shortInfoTitle;

    @Inject
    ProfilePresenter profilePresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    protected void injectComponent() {
        MainApplication.component().plus(new ProfileModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();

        profilePresenter.attachView(this);
        profilePresenter.initProfile();
    }

    @Override
    public void onDestroyView() {
        profilePresenter.detachView(this);
        super.onDestroyView();
    }

    private void initToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void showNameImageShortBio(@NotNull String fullName, @org.jetbrains.annotations.Nullable String imageLink, @NotNull String shortBio, boolean isMyProfile) {
        profileName.setText(fullName);
        Glide
                .with(getContext())
                .load(imageLink)
                .asBitmap()
                .placeholder(userPlaceholder)
                .into(profileImage);
        shortBioValue.setText(shortBio);

        if (isMyProfile) {
            shortBioTitle.setText(aboutMeTitle);
        } else {
            shortBioTitle.setText(shortInfoTitle);
        }

    }
}
