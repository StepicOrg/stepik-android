package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.ui.NotificationCategory;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class NotificationListFragment extends FragmentBase {

    private static final String categoryPositionKey = "categoryPositionKey";

    public static NotificationListFragment newInstance(int categoryPosition) {
        Bundle args = new Bundle();
        NotificationListFragment fragment = new NotificationListFragment();
        args.putInt(categoryPositionKey, categoryPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.notification_category)
    TextView test;

    NotificationCategory notificationCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("Create NOTIFICATION LIST Fragment");
        setRetainInstance(true);
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

        test.setText(notificationCategory.toString());
    }

}
