package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.SnackbarExtensionKt;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;

public class NotificationListFragment extends FragmentBase implements NotificationListView, SwipeRefreshLayout.OnRefreshListener {

    private static final String categoryPositionKey = "categoryPositionKey";
    private NotificationAdapter adapter;
    private final String loadingTag = "loadingTag";

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

    @BindView(R.id.load_progressbar)
    View progressBarOnEmptyScreen;

    @BindView(R.id.report_problem)
    View connectionProblemLayout;

    @BindView(R.id.empty_notifications)
    View emptyNotifications;

    @BindView(R.id.notification_swipe_refresh)
    SwipeRefreshLayout notificationSwipeRefresh;

    private RecyclerView.OnScrollListener recyclerViewScrollListener;
    private LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        int position = getArguments().getInt(categoryPositionKey);
        notificationCategory = NotificationCategory.values()[position];
        adapter = new NotificationAdapter(getContext(), notificationListPresenter, notificationCategory);
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
        return inflater.inflate(R.layout.fragment_notification_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Timber.d("We use notificationRecyclerView instance %s", sharedRecyclerViewPool);
        Timber.d("Our unique for fragment presenter is %s", notificationListPresenter);
//        notificationRecyclerView.setRecycledViewPool(sharedRecyclerViewPool); // TODO: 18.10.16 investigate why views is not rebind

        layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setRecycleChildrenOnDetach(true);
        notificationRecyclerView.setLayoutManager(layoutManager);
        notificationRecyclerView.setAdapter(adapter);

        initSwipeRefreshLayout();

        recyclerViewScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    Timber.d("visibleItemCount = %d, totalItemCount = %d, pastVisibleItems=%d", visibleItemCount, totalItemCount, pastVisibleItems);

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        if (notificationListPresenter.getWasShown().get() &&
                                !notificationListPresenter.isLoading().get() &&
                                notificationListPresenter.getHasNextPage().get()) {
                            Timber.d("try call loadMore()");
                            notificationListPresenter.loadMore();
                        }
                    }
                }

            }
        };
        notificationRecyclerView.addOnScrollListener(recyclerViewScrollListener);

        notificationListPresenter.attachView(this);
        notificationListPresenter.init(notificationCategory);
    }

    private void initSwipeRefreshLayout() {
        notificationSwipeRefresh.setOnRefreshListener(this);
        notificationSwipeRefresh.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

    }

    @Override
    public void onDestroyView() {
        notificationListPresenter.detachView(this);
        notificationRecyclerView.removeOnScrollListener(recyclerViewScrollListener);
        recyclerViewScrollListener = null;
        super.onDestroyView();
    }

    @Override
    public void onConnectionProblem() {
        adapter.showLoadingFooter(false);
        progressBarOnEmptyScreen.setVisibility(View.GONE);
        emptyNotifications.setVisibility(View.GONE);
        ProgressHelper.dismiss(notificationSwipeRefresh);
        if (adapter.getNotificationsCount() > 0) {
            //// TODO: 19.10.16 make in footer try again view
            showConnectionProblemMessage();
        } else {
            notificationRecyclerView.setVisibility(View.GONE);
            connectionProblemLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNeedShowNotifications(@NotNull List<Notification> notifications) {
        adapter.showLoadingFooter(false);
        progressBarOnEmptyScreen.setVisibility(View.GONE);
        connectionProblemLayout.setVisibility(View.GONE);
        ProgressHelper.dismiss(notificationSwipeRefresh);
        if (notifications.isEmpty()) {
            notificationRecyclerView.setVisibility(View.GONE);
            emptyNotifications.setVisibility(View.VISIBLE);
        } else {
            emptyNotifications.setVisibility(View.GONE);
            notificationRecyclerView.setVisibility(View.VISIBLE);
            adapter.setNotifications(notifications);
        }
    }

    @Override
    public void onLoading() {
        adapter.showLoadingFooter(false);
        notificationRecyclerView.setVisibility(View.GONE);
        connectionProblemLayout.setVisibility(View.GONE);
        emptyNotifications.setVisibility(View.GONE);
        progressBarOnEmptyScreen.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNeedShowLoadingFooter() {
        adapter.showLoadingFooter(true);
    }

    @Override
    public void notCheckNotification(int position, long notificationId) {
        showConnectionProblemMessage();
        adapter.markNotificationAsRead(position, notificationId, false);
    }

    @Override
    public void markNotificationAsRead(int position, long id) {
        adapter.markNotificationAsRead(position, id, true);
    }

    @Override
    public void onLoadingMarkingAsRead() {
        adapter.setEnableMarkAllButton(false);
        DialogFragment loadingProgressDialogFragment = LoadingProgressDialogFragment.Companion.newInstance();
        ProgressHelper.activate(loadingProgressDialogFragment, getFragmentManager(), loadingTag);
    }

    @Override
    public void makeEnableMarkAllButton() {
        adapter.setEnableMarkAllButton(true);
        ProgressHelper.dismiss(getFragmentManager(), loadingTag);
    }

    @Override
    public void markAsReadSuccessfully() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionProblemWhenMarkAllFail() {
        showConnectionProblemMessage();
    }

    private void showConnectionProblemMessage() {
        SnackbarExtensionKt
                .setTextColor(
                        Snackbar.make(notificationRecyclerView,
                                R.string.connectionProblems,
                                Snackbar.LENGTH_SHORT),
                        ColorUtil.INSTANCE.getColorArgb(R.color.white,
                                getContext()))
                .show();
    }

    @Override
    public void onRefresh() {
        boolean wasCancelled = notificationListPresenter.init(notificationCategory);
        if (wasCancelled) {
            ProgressHelper.dismiss(notificationSwipeRefresh);
        }
    }
}
