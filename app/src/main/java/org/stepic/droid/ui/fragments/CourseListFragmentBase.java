package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.checkout.Sku;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.Client;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.joining.contract.JoiningListener;
import org.stepic.droid.core.presenters.ContinueCoursePresenter;
import org.stepic.droid.core.presenters.DroppingPresenter;
import org.stepic.droid.core.presenters.contracts.ContinueCourseView;
import org.stepic.droid.core.presenters.contracts.CoursesView;
import org.stepic.droid.core.presenters.contracts.DroppingView;
import org.stepic.droid.model.CourseListType;
import org.stepik.android.domain.course_payments.model.CoursePayment;
import org.stepik.android.domain.last_step.model.LastStep;
import org.stepik.android.model.Course;
import org.stepic.droid.model.CoursesCarouselColorType;
import org.stepic.droid.ui.activities.contracts.RootScreen;
import org.stepic.droid.ui.adapters.CoursesAdapter;
import org.stepic.droid.ui.custom.StepikSwipeRefreshLayout;
import org.stepic.droid.ui.custom.TouchDispatchableFrameLayout;
import org.stepic.droid.ui.custom.WrapContentLinearLayoutManager;
import org.stepic.droid.ui.decorators.VerticalSpacesDecoration;
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment;
import org.stepic.droid.util.KotlinUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepikUtil;
import org.stepik.android.view.notification.delegate.RemindAppDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;

public abstract class CourseListFragmentBase extends FragmentBase
        implements SwipeRefreshLayout.OnRefreshListener,
        CoursesView,
        ContinueCourseView,
        JoiningListener,
        DroppingView {

    private static final String continueLoadingTag = "continueLoadingTag";

    @BindView(R.id.swipe_refresh_layout_mycourses)
    protected StepikSwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.list_of_courses)
    protected RecyclerView listOfCoursesView;

    @BindView(R.id.reportProblem)
    protected View reportConnectionProblem;

    @BindView(R.id.empty_courses)
    protected View emptyCoursesView;

    @BindView(R.id.empty_courses_anonymous_button)
    protected Button signInButton;

    @BindView(R.id.empty_courses_button)
    protected Button findCourseButton;

    @BindView(R.id.empty_courses_text)
    protected TextView emptyCoursesTextView;

    @BindView(R.id.root_fragment_view)
    protected TouchDispatchableFrameLayout rootView;

    @BindView(R.id.loadProgressbarOnEmptyScreen)
    protected ProgressBar progressBarOnEmptyScreen;

    @BindView(R.id.empty_search)
    protected ViewGroup emptySearch;

    @BindView(R.id.goToCatalog)
    protected Button goToCatalog;

    protected List<Course> courses;
    protected CoursesAdapter coursesAdapter;

    private RecyclerView.OnScrollListener listOfCoursesViewListener;
    private LinearLayoutManager layoutManager;

    @Inject
    protected ContinueCoursePresenter continueCoursePresenter;

    @Inject
    Client<JoiningListener> joiningListenerClient;

    @Inject
    protected DroppingPresenter droppingPresenter;

    @Inject
    protected RemindAppDelegate remindAppDelegate;

    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBarOnEmptyScreen.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(this);

        if (courses == null) courses = new ArrayList<>();
        boolean showMore = getCourseType() == CourseListType.ENROLLED;
        coursesAdapter = new CoursesAdapter(getActivity(), courses, continueCoursePresenter, droppingPresenter, true, showMore, CoursesCarouselColorType.Light);
        listOfCoursesView.setAdapter(coursesAdapter);
        layoutManager = new WrapContentLinearLayoutManager(getContext());
        listOfCoursesView.setLayoutManager(layoutManager);
        listOfCoursesView.addItemDecoration(new VerticalSpacesDecoration(getResources().getDimensionPixelSize(R.dimen.course_list_between_items_padding)));

        listOfCoursesViewListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount && StepikUtil.INSTANCE.isInternetAvailable()) {
                        onNeedDownloadNextPage();
                    }
                }

            }
        };
        listOfCoursesView.addOnScrollListener(listOfCoursesViewListener);
        registerForContextMenu(listOfCoursesView);


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnalytic().reportEvent(Analytic.Anonymous.AUTH_CENTER);
                getScreenManager().showLaunchScreen(getActivity());
            }
        });

        findCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity parent = getActivity();
                if (parent == null || !(parent instanceof RootScreen)) {
                    getScreenManager().showCatalog(getContext());
                } else {
                    getAnalytic().reportEvent(Analytic.Interaction.CLICK_FIND_COURSE_EMPTY_SCREEN);
                    if (getSharedPreferenceHelper().getAuthResponseFromStore() == null) {
                        getAnalytic().reportEvent(Analytic.Anonymous.BROWSE_COURSES_CENTER);
                    }
                    ((RootScreen) parent).showCatalog();
                }
            }
        });
        joiningListenerClient.subscribe(this);
        continueCoursePresenter.attachView(this);
        droppingPresenter.attachView(this);

        goToCatalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenManager.showCatalog(getContext());
            }
        });
    }

    @Override
    public void onDestroyView() {
        joiningListenerClient.unsubscribe(this);
        continueCoursePresenter.detachView(this);
        droppingPresenter.detachView(this);
        if (listOfCoursesView != null) {
            // do not set adapter to null, because fade out animation for fragment will not working
            unregisterForContextMenu(listOfCoursesView);
        }
        super.onDestroyView();
    }

    @Nullable
    protected abstract CourseListType getCourseType();

    public final void updateEnrollment(Course courseForUpdate, long enrollment) {
        boolean inList = false;
        int position = -1;
        for (int i = 0; i < courses.size(); i++) {
            Course courseItem = courses.get(i);
            if (courseItem.getId() == courseForUpdate.getId()) {
                courseItem.setEnrollment(courseItem.getId());
                courseForUpdate = courseItem;
                inList = true;
                position = i;
                break;
            }
        }
        if (getCourseType() == CourseListType.ENROLLED && !inList) {
            courses.add(courseForUpdate);
            coursesAdapter.notifyDataSetChanged();
        } else if (inList) {
            coursesAdapter.notifyItemChanged(position);
        }

    }

    @Override
    public void showLoading() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        reportConnectionProblem.setVisibility(View.GONE);

        if (courses.isEmpty()) {
            ProgressHelper.activate(swipeRefreshLayout);
            coursesAdapter.showLoadingFooter(false);
        } else if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            ProgressHelper.dismiss(swipeRefreshLayout);
            coursesAdapter.showLoadingFooter(true);
        } else {
            ProgressHelper.dismiss(swipeRefreshLayout);
            coursesAdapter.showLoadingFooter(false);
        }
    }

    @Override
    public void showEmptyCourses() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        reportConnectionProblem.setVisibility(View.GONE);
        if (courses.isEmpty()) {
            showEmptyScreen(true);
            remindAppDelegate.scheduleRemindAppNotification();
        }
    }

    @Override
    public void showConnectionProblem() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        coursesAdapter.showLoadingFooter(false);

        if (courses == null || courses.isEmpty()) {
            //screen is clear due to error connection
            showEmptyScreen(false);
            reportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public final void showCourses(@NonNull List<Course> courses, @NonNull Map<String, Sku> skus, @NonNull Map<Long, CoursePayment> coursePayments) {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        coursesAdapter.showLoadingFooter(false);
        reportConnectionProblem.setVisibility(View.GONE);
        showEmptyScreen(false);
        List<Course> finalCourses;
        int oldSize = this.courses.size();
        int updatedSize = courses.size();
        if (getCourseType() == null) {
            finalCourses = KotlinUtil.INSTANCE.getListOldPlusUpdated(this.courses, courses);
        } else {
            finalCourses = courses;
        }
        this.courses.clear();
        this.courses.addAll(finalCourses);

        coursesAdapter.setSkus(skus);
        coursesAdapter.setCoursePayments(coursePayments);
        coursesAdapter.notifyDataSetChanged();

        if (oldSize >= updatedSize) {
            onNeedDownloadNextPage();
        }
    }

    protected abstract void onNeedDownloadNextPage();

    protected abstract void showEmptyScreen(boolean isShown);

    @Override
    public void onShowContinueCourseLoadingDialog() {
        DialogFragment loadingProgressDialogFragment = LoadingProgressDialogFragment.Companion.newInstance();
        if (!loadingProgressDialogFragment.isAdded()) {
            loadingProgressDialogFragment.show(getFragmentManager(), continueLoadingTag);
        }
    }

    @Override
    public void onOpenStep(long courseId, LastStep lastStep) {
        ProgressHelper.dismiss(getFragmentManager(), continueLoadingTag);
        getScreenManager().continueCourse(getActivity(), courseId, lastStep.getUnit(), lastStep.getLesson(), lastStep.getStep());
    }

    @Override
    public void onOpenAdaptiveCourse(@NotNull Course course) {
        ProgressHelper.dismiss(getFragmentManager(), continueLoadingTag);
        getScreenManager().continueAdaptiveCourse(getActivity(), course);
    }

    @Override
    public void onAnyProblemWhileContinue(@NotNull Course course) {
        ProgressHelper.dismiss(getFragmentManager(), continueLoadingTag);
        getScreenManager().showCourseModules(getActivity(), course);
    }

    @Override
    public void onPause() {
        super.onPause();
        ProgressHelper.dismiss(getFragmentManager(), continueLoadingTag);
    }

    @Override
    public void onSuccessJoin(@Nullable Course joinedCourse) {
        updateEnrollment(joinedCourse, joinedCourse.getEnrollment());
    }

    @Override
    public final void onUserHasNotPermissionsToDrop() {
        Toast.makeText(getContext(), R.string.cant_drop, Toast.LENGTH_SHORT).show();
    }
}
