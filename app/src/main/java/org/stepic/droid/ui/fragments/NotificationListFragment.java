package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.presenters.NotificationListPresenter;
import org.stepic.droid.core.presenters.contracts.NotificationListView;
import org.stepic.droid.model.NotificationCategory;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.ui.adapters.NotificationAdapter;
import org.stepic.droid.ui.custom.StepikSwipeRefreshLayout;
import org.stepic.droid.ui.custom.StickyHeaderDecoration;
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepik.android.view.notification.FcmNotificationHandler;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import ru.nobird.android.view.base.ui.extension.SnackbarExtensionKt;
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

    @Inject
    NotificationListPresenter notificationListPresenter;

    @Inject
    FcmNotificationHandler fcmNotificationHandler;

    NotificationCategory notificationCategory;

    @BindView(R.id.notification_recycler_view)
    RecyclerView notificationRecyclerView;

    @BindView(R.id.loadProgressbarOnEmptyScreen)
    View progressBarOnEmptyScreen;

    @BindView(R.id.reportProblem)
    View connectionProblemLayout;

    @BindView(R.id.empty_notifications)
    View emptyNotifications;

    @BindView(R.id.goToCatalog)
    Button goToCatalog;

    @BindView(R.id.notification_swipe_refresh)
    StepikSwipeRefreshLayout notificationSwipeRefresh;

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
        App.Companion
                .component()
                .notificationsComponentBuilder()
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_list, container, false);
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
        notificationRecyclerView.addItemDecoration(new StickyHeaderDecoration<>(adapter));

        final RecyclerView.ItemAnimator notificationsItemAnimator = notificationRecyclerView.getItemAnimator();
        if (notificationsItemAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) notificationsItemAnimator).setSupportsChangeAnimations(false);
        }

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

        goToCatalog.setOnClickListener(v -> screenManager.showCatalog(getContext()));

        notificationListPresenter.attachView(this);
        notificationListPresenter.init(notificationCategory);
    }

    private void initSwipeRefreshLayout() {
        notificationSwipeRefresh.setOnRefreshListener(this);
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

    @Override
    public void openNotification(@NotNull Notification notification) {
        fcmNotificationHandler.tryOpenNotificationInstantly(requireContext(), notification);
    }

    private void showConnectionProblemMessage() {
        Snackbar.make(notificationRecyclerView,
                R.string.connectionProblems,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        boolean wasCancelled = notificationListPresenter.init(notificationCategory);
        if (wasCancelled) {
            ProgressHelper.dismiss(notificationSwipeRefresh);
        }
    }
}
