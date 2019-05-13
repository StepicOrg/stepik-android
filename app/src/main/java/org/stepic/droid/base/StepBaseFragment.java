package org.stepic.droid.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.NestedScrollView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.commentcount.contract.CommentCountListener;
import org.stepic.droid.core.presenters.AnonymousPresenter;
import org.stepic.droid.core.presenters.CommentsBannerPresenter;
import org.stepic.droid.core.presenters.RouteStepPresenter;
import org.stepic.droid.core.presenters.contracts.AnonymousView;
import org.stepic.droid.core.presenters.contracts.CommentsView;
import org.stepic.droid.core.presenters.contracts.RouteStepView;
import org.stepic.droid.persistence.model.StepPersistentWrapper;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.ui.custom.StepTextWrapper;
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment;
import org.stepic.droid.ui.dialogs.StepShareDialogFragment;
import org.stepic.droid.ui.util.PopupHelper;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DisplayUtils;
import org.stepic.droid.util.ProgressHelper;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Step;
import org.stepik.android.model.Unit;
import org.stepik.android.remote.step.model.StepResponse;
import org.stepik.android.view.ui.listener.FragmentViewPagerScrollStateListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import static org.stepic.droid.util.RxUtilKt.zip;

public abstract class StepBaseFragment extends FragmentBase
        implements RouteStepView,
        AnonymousView,
        CommentsView,
        CommentCountListener,
        FragmentViewPagerScrollStateListener {

    @BindView(R.id.open_comments_text)
    protected TextView textForComment;

    @BindView(R.id.auth_line_text)
    TextView authLineText;

    /**
     * default: Gone
     */
    @BindView(R.id.route_lesson_root)
    protected View routeLessonRoot;

    /**
     * do not make it gone, only invisible. Default: invisible
     */
    @BindView(R.id.next_lesson_view)
    protected View nextLessonView;

    /**
     * do not make it gone, only invisible. Default: invisible
     */
    @BindView(R.id.previous_lesson_view)
    protected View previousLessonView;

    @BindView(R.id.rootScrollView)
    @Nullable
    protected NestedScrollView nestedScrollView;

    protected StepPersistentWrapper stepWrapper;
    protected Step step;
    protected Lesson lesson;
    protected Section section;

    @Nullable
    protected Unit unit;

    private final static String LOAD_DIALOG_TAG = "stepBaseFragmentLoad";

    private final static String ROUTER_ROOT_VISIBILITY_KEY = "visibility_router_root";
    private final static String NEXT_LESSON_VISIBILITY_KEY = "visibility_next_lesson";
    private final static String PREVIOUS_LESSON_VISIBILITY_KEY = "visibility_previous_lesson";

    @Inject
    protected StepTextWrapper stepTextWrapper;

    @Inject
    protected RouteStepPresenter routeStepPresenter;

    @Inject
    AnonymousPresenter anonymousPresenter;

    @Inject
    CommentsBannerPresenter commentsBannerPresenter;

    @Inject
    Client<CommentCountListener> commentCountListenerClient;

    private CompositeDisposable uiCompositeDisposable = new CompositeDisposable();

    private BehaviorSubject<FragmentViewPagerScrollStateListener.ScrollState> fragmentVisibilitySubject =
        BehaviorSubject.create();

    private BehaviorSubject<Boolean> commentsVisibilitySubject =
        BehaviorSubject.createDefault(false);

    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .stepComponent(step.getId())
                .inject(this);
    }

    @Override
    protected final void onReleaseComponent() {
        App
                .Companion
                .componentManager()
                .releaseStepComponent(step.getId());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        stepWrapper = getArguments().getParcelable(AppConstants.KEY_STEP_BUNDLE);
        step = stepWrapper.getStep();
        lesson = getArguments().getParcelable(AppConstants.KEY_LESSON_BUNDLE);
        unit = getArguments().getParcelable(AppConstants.KEY_UNIT_BUNDLE);
        section = getArguments().getParcelable(AppConstants.KEY_SECTION_BUNDLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        stepTextWrapper.bind(step);

        updateCommentState();

        commentCountListenerClient.subscribe(this);
        routeStepPresenter.attachView(this);
        anonymousPresenter.attachView(this);
        commentsBannerPresenter.attachView(this);
        anonymousPresenter.checkForAnonymous();
        if (unit != null) {
            nextLessonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    routeStepPresenter.clickNextLesson(unit);
                }
            });
            previousLessonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    routeStepPresenter.clickPreviousLesson(unit);
                }
            });


            routeStepPresenter.checkStepForFirst(step.getId(), lesson, unit);
            routeStepPresenter.checkStepForLast(step.getId(), lesson, unit);
        }
    }

    protected abstract void attachStepTextWrapper();
    protected abstract void detachStepTextWrapper();
    @Override
    public void showCommentsBanner() {
        Observable<ScrollState> visibilityObservable =
            fragmentVisibilitySubject.filter(state -> state == ScrollState.ACTIVE);

        Observable<Boolean> commentsObservable =
            commentsVisibilitySubject.filter(isVisible -> isVisible);

        uiCompositeDisposable.add(zip(visibilityObservable, commentsObservable)
            .firstElement()
            .ignoreElement()
            .subscribe(() -> {
                View view = nestedScrollView.findViewById(R.id.open_comments_text);
                PopupHelper.INSTANCE.showPopupAnchoredToView(
                    getContext(),
                    view,
                    getString(R.string.step_comment_tooltip),
                    PopupHelper.PopupTheme.DARK_ABOVE,
                    true,
                    Gravity.TOP,
                    true,
                    true
                );
                commentsBannerPresenter.onBannerShown(section.getCourse());
            }));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attachStepTextWrapper();
        authLineText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreenManager().showLaunchScreen(getActivity());
            }
        });
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == (nestedScrollView.getChildAt(0).getMeasuredHeight() - nestedScrollView.getMeasuredHeight())) {
                    commentsVisibilitySubject.onNext(DisplayUtils.isVisible(nestedScrollView, textForComment));
                }
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            checkCommentsBanner();
        }
    }

    private void updateCommentState() {
        if (step != null && step.getDiscussionProxy() != null) {
            showComment();
        } else {
            textForComment.setVisibility(View.GONE);
        }
    }

    private void showComment() {
        textForComment.setVisibility(View.VISIBLE);
        textForComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int discussionCount = step.getDiscussionsCount();
                getAnalytic().reportEvent(Analytic.Comments.OPEN_FROM_STEP_UI);

                if (discussionCount == 0) {
                    getScreenManager().openComments(getActivity(), step.getDiscussionProxy(), step.getId(), true); //show new form, but in back stack comment oldList is exist.
                } else {
                    getScreenManager().openComments(getActivity(), step.getDiscussionProxy(), step.getId());
                }
            }
        });
        int discussionCount = step.getDiscussionsCount();
        if (discussionCount > 0) {
            textForComment.setText(App.Companion.getAppContext().getResources().getQuantityString(R.plurals.open_comments, discussionCount, discussionCount));
        } else {
            textForComment.setText(App.Companion.getAppContext().getResources().getString(R.string.open_comments_zero));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                getAnalytic().reportEvent(Analytic.Interaction.SHARE_STEP_CLICK);
                DialogFragment bottomSheetDialogFragment = StepShareDialogFragment.newInstance(step, lesson, unit);
                if (bottomSheetDialogFragment != null && !bottomSheetDialogFragment.isAdded()) {
                    bottomSheetDialogFragment.show(getFragmentManager(), null);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        detachStepTextWrapper();
        authLineText.setOnClickListener(null);
        textForComment.setOnClickListener(null);
        routeStepPresenter.detachView(this);
        commentCountListenerClient.unsubscribe(this);
        anonymousPresenter.detachView(this);
        commentsBannerPresenter.detachView(this);
        nextLessonView.setOnClickListener(null);
        previousLessonView.setOnClickListener(null);
        uiCompositeDisposable.clear();
        super.onDestroyView();
    }

    public void onDiscussionWasUpdatedFromInternet(Step updatedStep) {
        if (updatedStep.getId() == step.getId()) {
            step.setDiscussionProxy(updatedStep.getDiscussionProxy()); //fixme do it in immutable way
            step.setDiscussionsCount(updatedStep.getDiscussionsCount());
            updateCommentState();
        }
    }

    @Override
    public final void showNextLessonView() {
        routeLessonRoot.setVisibility(View.VISIBLE);
        nextLessonView.setVisibility(View.VISIBLE);
    }

    @Override
    public final void openNextLesson(@NotNull Unit nextUnit, @NotNull Lesson nextLesson, @NotNull Section nextSection) {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        getScreenManager().showSteps(getActivity(), nextUnit, nextLesson, nextSection);
        getActivity().finish();
    }

    @Override
    public void showLoading() {
        DialogFragment dialogFragment = LoadingProgressDialogFragment.Companion.newInstance();
        if (!dialogFragment.isAdded()) {
            dialogFragment.show(getFragmentManager(), LOAD_DIALOG_TAG);
        }
    }

    @Override
    public void showCantGoNext() {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        Toast.makeText(getContext(), R.string.cant_show_next_step, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPreviousLessonView() {
        routeLessonRoot.setVisibility(View.VISIBLE);
        previousLessonView.setVisibility(View.VISIBLE);
    }

    @Override
    public void openPreviousLesson(@NotNull Unit previousUnit, @NotNull Lesson previousLesson, @NotNull Section previousSection) {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        getScreenManager().showSteps(getActivity(), previousUnit, previousLesson, true, previousSection);
        getActivity().finish();
    }

    @Override
    public void showCantGoPrevious() {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        Toast.makeText(getContext(), R.string.cant_show_previous_step, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ROUTER_ROOT_VISIBILITY_KEY, routeLessonRoot.getVisibility() == View.VISIBLE);
        outState.putBoolean(NEXT_LESSON_VISIBILITY_KEY, nextLessonView.getVisibility() == View.VISIBLE);
        outState.putBoolean(PREVIOUS_LESSON_VISIBILITY_KEY, previousLessonView.getVisibility() == View.VISIBLE);
    }

    @Override
    public final void onShowAnonymous(boolean isAnonymous) {
        authLineText.setVisibility(isAnonymous ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSoftKeypad();
    }

    @Override
    public void onCommentCountUpdated() {
        long[] arr = new long[]{step.getId()};
        getApi().getSteps(arr)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StepResponseCallback(getThreadPoolExecutor(), getDatabaseFacade(), this));
    }


    //// TODO: 13.06.17 rework it in MVP style
    static class StepResponseCallback implements SingleObserver<StepResponse> {

        private final ThreadPoolExecutor threadPoolExecutor;
        private final DatabaseFacade databaseFacade;
        private final WeakReference<StepBaseFragment> stepBaseFragmentWeakReference;


        public StepResponseCallback(ThreadPoolExecutor threadPoolExecutor, DatabaseFacade databaseFacade, StepBaseFragment stepBaseFragment) {
            this.threadPoolExecutor = threadPoolExecutor;
            this.databaseFacade = databaseFacade;
            stepBaseFragmentWeakReference = new WeakReference<>(stepBaseFragment);
        }

        @Override
        public void onSubscribe(Disposable d) {}

        @Override
        public void onSuccess(StepResponse stepResponse) {
            if (stepResponse != null && stepResponse.getSteps() != null && !stepResponse.getSteps().isEmpty()) {
                final Step stepFromInternet = stepResponse.getSteps().get(0);
                if (stepFromInternet != null) {
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            databaseFacade.addStep(stepFromInternet); //fixme: fragment in closure -> leak
                        }
                    });


                    StepBaseFragment stepBaseFragment = stepBaseFragmentWeakReference.get();
                    if (stepBaseFragment != null) {
                        stepBaseFragment.onDiscussionWasUpdatedFromInternet(stepFromInternet);
                    }
                }
            }
        }

        @Override
        public void onError(Throwable e) {

        }
    }

    @Override
    public void onViewPagerScrollStateChanged(ScrollState scrollState) {
        changeVisibilitySubjects(scrollState);
    }

    private void changeVisibilitySubjects(ScrollState scrollState) {
        fragmentVisibilitySubject.onNext(scrollState);
        if (scrollState == ScrollState.ACTIVE && nestedScrollView != null) {
            commentsVisibilitySubject.onNext(DisplayUtils.isVisible(nestedScrollView, textForComment));
        }
    }

    private void checkCommentsBanner() {
        uiCompositeDisposable.add(Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (section != null && step.getDiscussionsCount() > 0) {
                        changeVisibilitySubjects(ScrollState.ACTIVE);
                        commentsBannerPresenter.fetchCommentsBanner(section.getCourse());
                    }
                }));
    }
}
