package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.presenters.DownloadingInteractionPresenter;
import org.stepic.droid.core.presenters.UnitsLearningProgressPresenter;
import org.stepic.droid.core.presenters.UnitsPresenter;
import org.stepic.droid.core.presenters.contracts.DownloadingInteractionView;
import org.stepic.droid.core.presenters.contracts.UnitsLearningProgressView;
import org.stepic.droid.core.presenters.contracts.UnitsView;
import org.stepic.droid.persistence.model.DownloadProgress;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Progress;
import org.stepik.android.model.Section;
import org.stepik.android.model.Unit;
import org.stepic.droid.ui.adapters.UnitAdapter;
import org.stepic.droid.ui.custom.StepikSwipeRefreshLayout;
import org.stepic.droid.ui.dialogs.DeleteItemDialogFragment;
import org.stepic.droid.ui.util.ToolbarHelperKt;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.SnackbarShower;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import timber.log.Timber;

public class UnitsFragment extends FragmentBase implements
        SwipeRefreshLayout.OnRefreshListener,
        UnitsView,
        DownloadingInteractionView,
        UnitsLearningProgressView {

    private static final int ANIMATION_DURATION = 0;
    public static final int DELETE_POSITION_REQUEST_CODE = 165;

    private final static String SECTION_KEY = "section_key";

    public static UnitsFragment newInstance(Section section) {
        Bundle args = new Bundle();
        args.putParcelable(SECTION_KEY, section);
        UnitsFragment fragment = new UnitsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @BindView(R.id.swipe_refresh_layout_units)
    StepikSwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.units_recycler_view)
    RecyclerView unitsRecyclerView;

    @BindView(R.id.loadProgressbarOnEmptyScreen)
    ProgressBar progressBar;

    @BindView(R.id.reportProblem)
    protected View reportConnectionProblem;

    @BindView(R.id.report_empty)
    protected View reportEmpty;

    @BindView(R.id.rootViewUnits)
    protected View rootView;

    private Section section;

    @Inject
    UnitsPresenter unitsPresenter;

    @Inject
    UnitsLearningProgressPresenter unitsLearningProgressPresenter;

    @Inject
    LessonSessionManager lessonManager;

    @Inject
    DownloadingInteractionPresenter downloadingInteractionPresenter;

    private UnitAdapter adapter;

    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .routingComponent()
                .sectionComponentBuilder()
                .build()
                .inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        section = getArguments().getParcelable(SECTION_KEY);
    }

    @Override
    protected void onReleaseComponent() {
        App.Companion
                .componentManager()
                .releaseRoutingComponent();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_units, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.share_menu, menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideSoftKeypad();
        swipeRefreshLayout.setOnRefreshListener(this);

        ToolbarHelperKt.initCenteredToolbar(this, R.string.units_lessons_title, true);

        unitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UnitAdapter(section, getAnalytic(), this, unitsPresenter);

        unitsRecyclerView.setAdapter(adapter);
        unitsRecyclerView.setItemAnimator(new SlideInRightAnimator());
        unitsRecyclerView.getItemAnimator().setRemoveDuration(ANIMATION_DURATION);
        unitsRecyclerView.getItemAnimator().setAddDuration(ANIMATION_DURATION);
        unitsRecyclerView.getItemAnimator().setMoveDuration(ANIMATION_DURATION);


        ProgressHelper.activate(progressBar);

        unitsPresenter.attachView(this);
        unitsLearningProgressPresenter.attachView(this);
        getLocalProgressManager().subscribe(unitsLearningProgressPresenter);
        unitsPresenter.showUnits(section, false);
    }

    @Override
    public void onDestroyView() {
        getLocalProgressManager().unsubscribe(unitsLearningProgressPresenter);
        unitsLearningProgressPresenter.detachView(this);
        unitsPresenter.detachView(this);

        lessonManager.reset();

        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        downloadingInteractionPresenter.attachView(this);
    }

    @Override
    public void onStop() {
        downloadingInteractionPresenter.detachView(this);
        super.onStop();
        ProgressHelper.dismiss(swipeRefreshLayout);
    }

    @Override
    public void onRefresh() {
        getAnalytic().reportEvent(Analytic.Interaction.REFRESH_UNITS);
        unitsPresenter.showUnits(section, true);
    }

    @org.jetbrains.annotations.Nullable
    private Pair<Unit, Integer> getUnitOnScreenAndPositionById(long unitId) {
        int position = -1;
        Unit unit = null;
        for (int i = 0; i < adapter.getUnits().size(); i++) {
            if (adapter.getUnits().get(i).getId() == unitId) {
                position = i;
                unit = adapter.getUnits().get(i);
                break;
            }
        }
        if (unit == null || position == -1 || position >= adapter.getUnits().size()) return null;
        return new Pair<>(unit, position);
    }

    private void shareSection() {
        if (section != null) {
            Intent intent = getShareHelper().getIntentForSectionSharing(section);
            startActivity(intent);
        }
    }

    private void dismiss() {
        ProgressHelper.dismiss(progressBar);
        ProgressHelper.dismiss(swipeRefreshLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                shareSection();
                return true;
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                getActivity().finish();
                return true;
        }
        return false;
    }

    @Override
    public void onEmptyUnits() {
        reportConnectionProblem.setVisibility(View.GONE);
        dismiss();
        reportEmpty.setVisibility(View.VISIBLE);
    }

    public void onNeedShowUnits(@NotNull List<Unit> unitList, @NotNull List<Lesson> lessonList, @NotNull Map<Long, Progress> progressMap) {
        reportEmpty.setVisibility(View.GONE);
        reportConnectionProblem.setVisibility(View.GONE);

        adapter.getLessons().clear();
        adapter.getLessons().addAll(lessonList);

        adapter.getUnits().clear();
        adapter.getUnits().addAll(unitList);

        adapter.getUnitProgressMap().clear();
        for (Map.Entry<Long, Progress> pair: progressMap.entrySet()) {
            adapter.getUnitProgressMap().append(pair.getKey(), pair.getValue());
        }

        adapter.notifyDataSetChanged();

        dismiss();
    }

    @Override
    public void onLoading() {
        reportEmpty.setVisibility(View.GONE);
        reportConnectionProblem.setVisibility(View.GONE);
        if (adapter.getItemCount() == 0) {
            ProgressHelper.activate(progressBar);
        }
    }

    @Override
    public void onConnectionProblem() {
        dismiss();
        if (adapter.getItemCount() == 0) {
            reportEmpty.setVisibility(View.GONE);
            reportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (adapter != null && requestCode == DELETE_POSITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            getAnalytic().reportEvent(Analytic.Interaction.ACCEPT_DELETING_UNIT);
            int position = data.getIntExtra(DeleteItemDialogFragment.deletePositionKey, -1);
            unitsPresenter.removeDownloadTask(adapter.getUnits().get(position));
        }

//        if (requestCode == VideoQualityDetailedDialog.VIDEO_QUALITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            int position = data.getIntExtra(VideoQualityDetailedDialog.POSITION_KEY, -1);
//            determineNetworkTypeAndLoad(position);
//        }
    }

    @Override
    public void determineNetworkTypeAndLoad(int position) {
        downloadingInteractionPresenter.checkOnLoading(position);
    }

    @Override
    public void onLoadingAccepted(int position) {
        unitsPresenter.addDownloadTask(adapter.getUnits().get(position));
    }

    @Override
    public void showOnRemoveDownloadDialog(int position) {
        DeleteItemDialogFragment dialogFragment = DeleteItemDialogFragment.newInstance(position);
        dialogFragment.setTargetFragment(this, UnitsFragment.DELETE_POSITION_REQUEST_CODE);
        dialogFragment.show(getFragmentManager(), DeleteItemDialogFragment.TAG);
    }

    @Override
    public void onShowPreferenceSuggestion() {
        getAnalytic().reportEvent(Analytic.Downloading.SHOW_SNACK_PREFS_UNITS);
        SnackbarShower.INSTANCE.showTurnOnDownloadingInSettings(rootView, getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getAnalytic().reportEvent(Analytic.Downloading.CLICK_SETTINGS_UNITS);
                    getScreenManager().showSettings(getActivity());
                } catch (NullPointerException nullPointerException) {
                    Timber.e(nullPointerException);
                }
            }
        });
    }

    @Override
    public void onShowInternetIsNotAvailableRetry(final int position) {
        getAnalytic().reportEvent(Analytic.Downloading.SHOW_SNACK_INTERNET_UNITS);
        SnackbarShower.INSTANCE.showInternetRetrySnackbar(rootView, getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnalytic().reportEvent(Analytic.Downloading.CLICK_RETRY_UNITS);
                if (adapter != null) {
                    adapter.onItemDownloadClicked(position);
                }
            }
        });
    }

    @Override
    public void setNewScore(long unitId, double newScore) {
        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;

        int position = unitPairPosition.second;

        Progress progress = adapter.getUnitProgressMap().get(unitId);
        if (progress != null) {
            progress.setScore(newScore + "");
        }

        adapter.notifyItemChanged(position);
    }

    @Override
    public void setUnitPassed(long unitId) {
        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        adapter.notifyItemChanged(position);
    }

    @Override
    public void showDownloadProgress(@NotNull DownloadProgress progress) {
        if (adapter != null) {
            adapter.setItemDownloadProgress(progress);
        }
    }

    public void openSteps(@NotNull Unit unit, @NotNull Lesson lesson, @org.jetbrains.annotations.Nullable Section parentSection) {
        screenManager.showSteps(getActivity(), unit, lesson, parentSection);
    }

    @Override
    public void showVideoQualityDialog(int position) {
//        VideoQualityDetailedDialog dialogFragment = VideoQualityDetailedDialog.Companion.newInstance(position);
//        dialogFragment.setTargetFragment(this, VideoQualityDetailedDialog.VIDEO_QUALITY_REQUEST_CODE);
//        dialogFragment.show(getFragmentManager(), VideoQualityDetailedDialog.TAG);
    }
}
