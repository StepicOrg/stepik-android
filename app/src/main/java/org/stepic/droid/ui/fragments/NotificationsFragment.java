package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.ui.NotificationCategory;
import org.stepic.droid.ui.activities.HasDrawer;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class NotificationsFragment extends FragmentBase {

    private ActionBarDrawerToggle actionBarDrawerToggle;

    public static NotificationsFragment newInstance() {
        Bundle args = new Bundle();
        NotificationsFragment fragment = new NotificationsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.toolbar_fragment)
    Toolbar toolbar;

    @BindView(R.id.notification_tabs)
    TabLayout tabLayout;

    @BindView(R.id.notification_viewpager)
    ViewPager viewPager;

    private HasDrawer hasDrawerHost;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HasDrawer) {
            hasDrawerHost = (HasDrawer) context;
        }
    }

    @Override
    public void onDetach() {
        hasDrawerHost = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();
        initViewPager();
    }

    private void initViewPager() {
        viewPager.setAdapter(new NotificationPagerAdapter(getFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroyView() {
        destroyToolbar();
        super.onDestroyView();
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.notification_title);
        if (hasDrawerHost != null) {
            actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), hasDrawerHost.getDrawerLayout(), toolbar, R.string.drawer_open, R.string.drawer_closed);
            hasDrawerHost.getDrawerLayout().addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
        }
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
    }

    private void destroyToolbar() {
        if (actionBarDrawerToggle != null && hasDrawerHost != null) {
            hasDrawerHost.getDrawerLayout().removeDrawerListener(actionBarDrawerToggle);
        }
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.show();
        }
    }


    static class NotificationPagerAdapter extends FragmentStatePagerAdapter {
        private final int numberOfCategories = NotificationCategory.values().length;

        public NotificationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Timber.d("getItem %d", position);
            return NotificationListFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return numberOfCategories;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Timber.d("getPageTitle %d", position);
            int resString = NotificationCategory.values()[position].getTitle();
            return MainApplication.getAppContext().getString(resString);
        }
    }

}
