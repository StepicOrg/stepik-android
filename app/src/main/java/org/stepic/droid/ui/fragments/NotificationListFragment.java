package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.modules.NotificationModule;
import org.stepic.droid.core.presenters.NotificationListPresenter;
import org.stepic.droid.core.presenters.contracts.NotificationListView;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.ui.NotificationCategory;
import org.stepic.droid.ui.adapters.NotificationAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class NotificationListFragment extends FragmentBase implements NotificationListView {

    private static final String categoryPositionKey = "categoryPositionKey";
    private NotificationAdapter adapter;

    public static NotificationListFragment newInstance(int categoryPosition) {
        Bundle args = new Bundle();
        NotificationListFragment fragment = new NotificationListFragment();
        args.putInt(categoryPositionKey, categoryPosition);
        fragment.setArguments(args);
        return fragment;
    }

//    @Inject
//    RecyclerView.RecycledViewPool sharedRecyclerViewPool;

    @Inject
    NotificationListPresenter notificationListPresenter;

    NotificationCategory notificationCategory;

    @BindView(R.id.notification_recycler_view)
    RecyclerView notificationRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new NotificationAdapter(getContext());
    }

    @Override
    protected void injectComponent() {
        MainApplication
                .component()
                .plus(new NotificationModule())
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int position = getArguments().getInt(categoryPositionKey);
        notificationCategory = NotificationCategory.values()[position];

//        Timber.d("We use notificationRecyclerView instance %s", sharedRecyclerViewPool);
        Timber.d("Our unique for fragment presenter is %s", notificationListPresenter);
//        notificationRecyclerView.setRecycledViewPool(sharedRecyclerViewPool); // TODO: 18.10.16 investigate why views is not rebind

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setRecycleChildrenOnDetach(true);
        notificationRecyclerView.setLayoutManager(layoutManager);
        notificationRecyclerView.setAdapter(adapter);

        notificationListPresenter.attachView(this);
        notificationListPresenter.init(notificationCategory);
    }

    @Override
    public void onDestroyView() {
        notificationListPresenter.detachView(this);
        super.onDestroyView();
    }

    @Override
    public void onConnectionProblem() {
        Toast.makeText(getContext(), "Connection problem...", Toast.LENGTH_SHORT).show(); //// FIXME: 17.10.16 make ok UI
    }

    @Override
    public void onNeedShowNotifications(@NotNull List<Notification> notifications) {
        if (notifications.isEmpty()) {
            notificationRecyclerView.setVisibility(View.GONE);
            //// FIXME: 18.10.16 add placeholder
        } else {
            notificationRecyclerView.setVisibility(View.VISIBLE);
            adapter.setNotifications(notifications);
        }
    }

    @Override
    public void onLoading() {
        Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show(); //// FIXME: 17.10.16 make ok UI
    }
}
