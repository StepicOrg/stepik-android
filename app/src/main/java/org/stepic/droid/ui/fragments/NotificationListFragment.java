package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.components.NotificationComponent;
import org.stepic.droid.core.presenters.NotificationListPresenter;
import org.stepic.droid.core.presenters.contracts.NotificationListView;
import org.stepic.droid.ui.NotificationCategory;
import org.stepic.droid.ui.adapters.NotificationAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationListFragment extends FragmentBase implements NotificationListView {

    private static final String categoryPositionKey = "categoryPositionKey";

    public static NotificationListFragment newInstance(int categoryPosition) {
        Bundle args = new Bundle();
        NotificationListFragment fragment = new NotificationListFragment();
        args.putInt(categoryPositionKey, categoryPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    RecyclerView.RecycledViewPool recycledViewPool;

    @Inject
    NotificationListPresenter notificationListPresenter;

    NotificationCategory notificationCategory;

    @BindView(R.id.mark_all_as_read_button)
    View markAsReadButton;

    @BindView(R.id.notification_recycler_view)
    RecyclerView notificationRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        NotificationComponent component = getComponentFromActivity(NotificationComponent.class);
        component.inject(this);
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setRecycleChildrenOnDetach(true);
        notificationRecyclerView.setItemViewCacheSize(10);

        notificationRecyclerView.setLayoutManager(layoutManager);
        notificationRecyclerView.setRecycledViewPool(recycledViewPool);
        notificationRecyclerView.setAdapter(new NotificationAdapter(getContext()));
    }

}
