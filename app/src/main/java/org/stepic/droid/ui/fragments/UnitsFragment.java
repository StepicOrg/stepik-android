package org.stepic.droid.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.RoutingConsumer;
import org.stepic.droid.core.presenters.DownloadingInteractionPresenter;
import org.stepic.droid.core.presenters.DownloadingProgressUnitsPresenter;
import org.stepic.droid.core.presenters.UnitsLearningProgressPresenter;
import org.stepic.droid.core.presenters.UnitsPresenter;
import org.stepic.droid.core.presenters.contracts.DownloadingInteractionView;
import org.stepic.droid.core.presenters.contracts.DownloadingProgressUnitsView;
import org.stepic.droid.core.presenters.contracts.UnitsLearningProgressView;
import org.stepic.droid.core.presenters.contracts.UnitsView;
import org.stepic.droid.events.units.LessonCachedEvent;
import org.stepic.droid.events.units.NotCachedLessonEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.LessonLoadingState;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.ui.adapters.UnitAdapter;
import org.stepic.droid.ui.dialogs.DeleteItemDialogFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.SnackbarShower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import timber.log.Timber;

public class UnitsFragment extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener, UnitsView, DownloadingProgressUnitsView, DownloadingInteractionView, UnitsLearningProgressView, RoutingConsumer.Listener {

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
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.units_recycler_view)
    RecyclerView unitsRecyclerView;

    @BindView(R.id.load_progressbar)
    ProgressBar progressBar;

    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;

    @BindView(R.id.report_problem)
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
    DownloadingProgressUnitsPresenter downloadingProgressUnitsPresenter;

    @Inject
    DownloadingInteractionPresenter downloadingInteractionPresenter;

    @Inject
    RoutingConsumer routingConsumer;

    UnitAdapter adapter;

    private List<Unit> unitList;
    private List<Lesson> lessonList;
    private Map<Long, Progress> progressMap;
    private Map<Long, LessonLoadingState> lessonIdToLoadingStateMap;

    @Override
    protected void injectComponent() {
        App.Companion
                .getComponentManager()
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
        lessonIdToLoadingStateMap = new HashMap<>();
    }

    @Override
    protected void onReleaseComponent() {
        App.Companion
                .getComponentManager().releaseRoutingComponent();
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
        swipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        unitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        unitList = new ArrayList<>();
        lessonList = new ArrayList<>();
        progressMap = new HashMap<>();
        adapter = new UnitAdapter(section,
                unitList,
                lessonList,
                progressMap,
                (AppCompatActivity) getActivity(),
                lessonIdToLoadingStateMap,
                this,
                downloadingInteractionPresenter);

        unitsRecyclerView.setAdapter(adapter);
        unitsRecyclerView.setItemAnimator(new SlideInRightAnimator());
        unitsRecyclerView.getItemAnimator().setRemoveDuration(ANIMATION_DURATION);
        unitsRecyclerView.getItemAnimator().setAddDuration(ANIMATION_DURATION);
        unitsRecyclerView.getItemAnimator().setMoveDuration(ANIMATION_DURATION);


        ProgressHelper.activate(progressBar);

        bus.register(this);
        unitsPresenter.attachView(this);
        unitsLearningProgressPresenter.attachView(this);
        localProgressManager.subscribe(unitsLearningProgressPresenter);
        routingConsumer.subscribe(this);
        unitsPresenter.showUnits(section, false);

    }

    @Override
    public void onDestroyView() {
        routingConsumer.unsubscribe(this);
        localProgressManager.unsubscribe(unitsLearningProgressPresenter);
        unitsLearningProgressPresenter.detachView(this);
        unitsPresenter.detachView(this);
        bus.unregister(this);

        lessonManager.reset();

        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("downloading interaction presenter instance: %s", downloadingInteractionPresenter);
        downloadingInteractionPresenter.attachView(this);
        downloadingProgressUnitsPresenter.attachView(this);
        downloadingProgressUnitsPresenter.subscribeToProgressUpdates(lessonList);
    }

    @Override
    public void onStop() {
        downloadingInteractionPresenter.detachView(this);
        downloadingProgressUnitsPresenter.detachView(this);
        super.onStop();
        ProgressHelper.dismiss(swipeRefreshLayout);
    }

    @Override
    public void onRefresh() {
        analytic.reportEvent(Analytic.Interaction.REFRESH_UNIT);
        unitsPresenter.showUnits(section, true);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE && permissions.length > 0) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                int position = sharedPreferenceHelper.getTempPosition();
                if (adapter != null) {
                    adapter.requestClickLoad(position);
                }
            }
        }
    }

    private void updateState(long lessonId, boolean isCached, boolean isLoading) {
        int position = -1;
        Lesson lesson = null;
        for (int i = 0; i < lessonList.size(); i++) {
            if (lessonList.get(i).getId() == lessonId) {
                position = i;
                lesson = lessonList.get(i);
                break;
            }
        }
        if (lesson == null || position == -1 || position >= lessonList.size()) return;

        lesson.set_cached(isCached);
        lesson.set_loading(isLoading);
        adapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onNotCachedSection(NotCachedLessonEvent e) {
        long unitId = e.getLessonId();
        updateState(unitId, false, false);
    }

    @org.jetbrains.annotations.Nullable
    private Pair<Unit, Integer> getUnitOnScreenAndPositionById(long unitId) {
        int position = -1;
        Unit unit = null;
        for (int i = 0; i < unitList.size(); i++) {
            if (unitList.get(i).getId() == unitId) {
                position = i;
                unit = unitList.get(i);
                break;
            }
        }
        if (unit == null || position == -1 || position >= unitList.size()) return null;
        return new Pair<>(unit, position);
    }

    @Subscribe
    public void onLessonCachedEvent(LessonCachedEvent e) {
        updateState(e.getLessonId(), true, false);
    }

    private void shareSection() {
        if (section != null) {
            Intent intent = shareHelper.getIntentForSectionSharing(section);
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

        this.lessonList.clear();
        this.lessonList.addAll(lessonList);

        this.unitList.clear();
        this.unitList.addAll(unitList);

        this.progressMap.clear();
        this.progressMap.putAll(progressMap);

        adapter.notifyDataSetChanged();

        dismiss();

        downloadingProgressUnitsPresenter.subscribeToProgressUpdates(lessonList);
    }

    @Override
    public void onLoading() {
        reportEmpty.setVisibility(View.GONE);
        reportConnectionProblem.setVisibility(View.GONE);
        if (unitList.isEmpty()) {
            ProgressHelper.activate(progressBar);
        }
    }

    @Override
    public void onConnectionProblem() {
        if (unitList.isEmpty()) {
            dismiss();
            reportEmpty.setVisibility(View.GONE);
            reportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNewProgressValue(@NonNull LessonLoadingState lessonLoadingState) {
//// FIXME: 20.02.17 store hash map, because it is executed each 300 ms
        int position = -1;
        for (int i = 0; i < lessonList.size(); i++) {
            Lesson lesson = lessonList.get(i);
            if (lesson.getId() == lessonLoadingState.getLessonId()) {
                position = i;
            }
        }

        if (position < 0) {
            return;
        }

        //change state for updating in adapter
        lessonIdToLoadingStateMap.put(lessonLoadingState.getLessonId(), lessonLoadingState);

        adapter.notifyItemChanged(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (adapter != null && requestCode == DELETE_POSITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            analytic.reportEvent(Analytic.Interaction.ACCEPT_DELETING_UNIT);
            int position = data.getIntExtra(DeleteItemDialogFragment.deletePositionKey, -1);
            adapter.requestClickDeleteSilence(position);
        }
    }

    @Override
    public void onLoadingAccepted(int position) {
        adapter.loadAfterDetermineNetworkState(position);
    }

    @Override
    public void onShowPreferenceSuggestion() {
        analytic.reportEvent(Analytic.Downloading.SHOW_SNACK_PREFS_UNITS);
        SnackbarShower.INSTANCE.showTurnOnDownloadingInSettings(rootView, getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    analytic.reportEvent(Analytic.Downloading.CLICK_SETTINGS_UNITS);
                    screenManager.showSettings(getActivity());
                } catch (NullPointerException nullPointerException) {
                    Timber.e(nullPointerException);
                }
            }
        });
    }

    @Override
    public void onShowInternetIsNotAvailableRetry(final int position) {
        analytic.reportEvent(Analytic.Downloading.SHOW_SNACK_INTERNET_UNITS);
        SnackbarShower.INSTANCE.showInternetRetrySnackbar(rootView, getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analytic.reportEvent(Analytic.Downloading.CLICK_RETRY_UNITS);
                if (adapter != null) {
                    adapter.requestClickLoad(position);
                }
            }
        });
    }

    @Override
    public void setNewScore(long unitId, double newScore) {
        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;

        int position = unitPairPosition.second;

        Progress progress = progressMap.get(unitId);
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

        unit.set_viewed_custom(true);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onSectionChanged(@NotNull Section oldSection, @NotNull Section newSection) {
        if (section != null && oldSection.getId() == section.getId()) {
            section = newSection;
            adapter.setSection(section);
            unitsPresenter.showUnits(newSection, true);
        }
    }
}
