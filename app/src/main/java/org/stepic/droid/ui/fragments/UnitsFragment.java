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

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.Client;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.downloadingstate.DownloadingPresenter;
import org.stepic.droid.core.downloadingstate.DownloadingView;
import org.stepic.droid.core.presenters.DownloadingInteractionPresenter;
import org.stepic.droid.core.presenters.UnitsLearningProgressPresenter;
import org.stepic.droid.core.presenters.UnitsPresenter;
import org.stepic.droid.core.presenters.contracts.DownloadingInteractionView;
import org.stepic.droid.core.presenters.contracts.UnitsLearningProgressView;
import org.stepic.droid.core.presenters.contracts.UnitsView;
import org.stepic.droid.core.routing.contract.RoutingListener;
import org.stepic.droid.model.Lesson;
import org.stepik.android.model.structure.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.storage.StoreStateManager;
import org.stepic.droid.ui.adapters.UnitAdapter;
import org.stepic.droid.ui.custom.StepikSwipeRefreshLayout;
import org.stepic.droid.ui.dialogs.DeleteItemDialogFragment;
import org.stepic.droid.ui.util.ToolbarHelperKt;
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

public class UnitsFragment extends FragmentBase implements
        SwipeRefreshLayout.OnRefreshListener,
        UnitsView,
        DownloadingView,
        DownloadingInteractionView,
        UnitsLearningProgressView,
        RoutingListener,
        StoreStateManager.LessonCallback {

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
    DownloadingPresenter downloadingPresenter;

    @Inject
    DownloadingInteractionPresenter downloadingInteractionPresenter;

    @Inject
    Client<RoutingListener> routingClient;

    @Inject
    StoreStateManager storeStateManager;

    private UnitAdapter adapter;

    private List<Unit> unitList;
    private List<Lesson> lessonList;
    private Map<Long, Progress> progressMap;
    private Map<Long, Float> lessonIdToLoadingStateMap;

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
        lessonIdToLoadingStateMap = new HashMap<>();
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
        unitList = new ArrayList<>();
        lessonList = new ArrayList<>();
        progressMap = new HashMap<>();
        adapter = new UnitAdapter(section,
                downloadingPresenter,
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

        storeStateManager.addLessonCallback(this);
        downloadingPresenter.attachView(this);
        unitsPresenter.attachView(this);
        unitsLearningProgressPresenter.attachView(this);
        getLocalProgressManager().subscribe(unitsLearningProgressPresenter);
        routingClient.subscribe(this);
        unitsPresenter.showUnits(section, false);
    }

    @Override
    public void onDestroyView() {
        downloadingPresenter.detachView(this);
        routingClient.unsubscribe(this);
        getLocalProgressManager().unsubscribe(unitsLearningProgressPresenter);
        unitsLearningProgressPresenter.detachView(this);
        unitsPresenter.detachView(this);
        storeStateManager.removeLessonCallback(this);

        lessonManager.reset();

        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        downloadingInteractionPresenter.attachView(this);
        for (Lesson lesson : lessonList) {
            downloadingPresenter.onStateChanged(lesson.getId(), lesson.is_loading());
        }
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE && permissions.length > 0) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                int position = getSharedPreferenceHelper().getTempPosition();
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
        downloadingPresenter.onStateChanged(lessonId, isLoading);
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

        this.lessonList.clear();
        this.lessonList.addAll(lessonList);

        this.unitList.clear();
        this.unitList.addAll(unitList);

        this.progressMap.clear();
        this.progressMap.putAll(progressMap);

        adapter.notifyDataSetChanged();

        dismiss();

        for (Lesson lesson : this.lessonList) {
            downloadingPresenter.onStateChanged(lesson.getId(), lesson.is_loading());
        }
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
        dismiss();
        if (unitList.isEmpty()) {
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
            adapter.requestClickDeleteSilence(position);
        }
    }

    @Override
    public void onLoadingAccepted(int position) {
        adapter.loadAfterDetermineNetworkState(position);
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

    @Override
    public void onLessonCached(long lessonId) {
        updateState(lessonId, true, false);
    }

    @Override
    public void onLessonNotCached(long lessonId) {
        updateState(lessonId, false, false);
    }

    @Override
    public void onNewProgressValue(long id, float portion) {
        int position = -1;
        for (int i = 0; i < lessonList.size(); i++) {
            Lesson lesson = lessonList.get(i);
            if (lesson.getId() == id) {
                position = i;
            }
        }

        if (position < 0) {
            return;
        }

        //change state for updating in adapter
        lessonIdToLoadingStateMap.put(id, portion);

        adapter.notifyItemChanged(position);
    }
}
