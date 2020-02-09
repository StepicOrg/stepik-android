package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.AmplitudeAnalytic;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.model.NotificationCategory;
import org.stepic.droid.ui.util.ToolbarHelperKt;

import butterknife.BindView;
import timber.log.Timber;

public class NotificationsFragment extends FragmentBase {

    @NotNull
    public static NotificationsFragment newInstance() {
        Bundle args = new Bundle();
        NotificationsFragment fragment = new NotificationsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.notification_tabs)
    TabLayout tabLayout;

    @BindView(R.id.notification_viewpager)
    ViewPager viewPager;

    @BindView(R.id.needAuthView)
    View needAuthRootView;

    @BindView(R.id.authAction)
    Button authUserButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Notifications.NOTIFICATION_SCREEN_OPENED);
        analytic.reportEvent(Analytic.Notification.NOTIFICATION_SCREEN_OPENED);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getSharedPreferenceHelper().getAuthResponseFromStore() == null) {
            authUserButton.setOnClickListener(v -> getScreenManager().showLaunchScreen(getActivity()));
//            toolbar.setVisibility(View.GONE); // FIXME: 15.08.17 hide, when it is needed
            initToolbar();
            tabLayout.setVisibility(View.GONE);
            needAuthRootView.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            initToolbar();
//            toolbar.setVisibility(View.VISIBLE);
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
        authUserButton.setOnClickListener(null);
        super.onDestroyView();
    }

    private void initToolbar() {
        ToolbarHelperKt.initCenteredToolbar(this, R.string.notification_title);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notification_center_menu, menu);
        menu.findItem(R.id.action_settings).setVisible(getSharedPreferenceHelper().getAuthResponseFromStore() != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                getAnalytic().reportEvent(Analytic.Interaction.CLICK_SETTINGS_FROM_NOTIFICATION);
                getScreenManager().showNotificationSettings(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class NotificationPagerAdapter extends FragmentStatePagerAdapter {
        private final int numberOfCategories = NotificationCategory.values().length;

        public NotificationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        @NonNull
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
            return App.Companion.getAppContext().getString(resString);
        }
    }

}
