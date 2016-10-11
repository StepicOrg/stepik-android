package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
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

    @Override
    public void onDestroyView() {
        Timber.d("onDestroyView");
        if (actionBarDrawerToggle != null && hasDrawerHost != null) {
            hasDrawerHost.getDrawerLayout().removeDrawerListener(actionBarDrawerToggle);
        }
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.show();
        }
        super.onDestroyView();
    }
}
