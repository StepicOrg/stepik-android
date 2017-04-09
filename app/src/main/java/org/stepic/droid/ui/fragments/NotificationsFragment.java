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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.ui.NotificationCategory;
import org.stepic.droid.ui.activities.HasDrawer;

import butterknife.BindView;
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

    @BindView(R.id.need_auth_view)
    View needAuthRootView;

    @BindView(R.id.auth_action)
    Button authUserButton;

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
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (sharedPreferenceHelper.getAuthResponseFromStore() == null) {
            authUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    screenManager.showLaunchScreen(getActivity());
                }
            });
            toolbar.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            needAuthRootView.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            initToolbar();
            toolbar.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            needAuthRootView.setVisibility(View.GONE);
            initViewPager();
        }
    }

    private void initViewPager() {
        viewPager.setAdapter(new NotificationPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroyView() {
        destroyToolbar();
        authUserButton.setOnClickListener(null);
        super.onDestroyView();
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.notification_title);
        if (hasDrawerHost != null) {
            actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), hasDrawerHost.getDrawerLayout(), toolbar, R.string.drawer_open, R.string.drawer_closed);
            hasDrawerHost.getDrawerLayout().addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();

            toolbar.inflateMenu(R.menu.notification_center_menu);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_settings:
                            analytic.reportEvent(Analytic.Interaction.CLICK_SETTINGS_FROM_NOTIFICATION);
                            screenManager.showSettings(getActivity());
                            return true;
                    }
                    return false;
                }
            });
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
        toolbar.setOnMenuItemClickListener(null);
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
            return App.getAppContext().getString(resString);
        }
    }

}
