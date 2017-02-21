package org.stepic.droid.ui.fragments;

import android.Manifest;
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
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.modules.UnitsModule;
import org.stepic.droid.core.presenters.DownloadingProgressUnitsPresenter;
import org.stepic.droid.core.presenters.UnitsPresenter;
import org.stepic.droid.core.presenters.contracts.DownloadingProgressUnitsView;
import org.stepic.droid.core.presenters.contracts.UnitsView;
import org.stepic.droid.events.units.NotCachedUnitEvent;
import org.stepic.droid.events.units.UnitCachedEvent;
import org.stepic.droid.events.units.UnitProgressUpdateEvent;
import org.stepic.droid.events.units.UnitScoreUpdateEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.LessonLoadingState;
import org.stepic.droid.ui.adapters.UnitAdapter;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import timber.log.Timber;

public class UnitsFragment extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener, UnitsView, DownloadingProgressUnitsView {

    private static final int ANIMATION_DURATION = 0;

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

    private Section section;

    @Inject
    UnitsPresenter unitsPresenter;

    @Inject
    LessonSessionManager lessonManager;

    @Inject
    DownloadingProgressUnitsPresenter downloadingProgressUnitsPresenter;

    UnitAdapter adapter;

    private List<Unit> unitList;
    private List<Lesson> lessonList;
    private Map<Long, Progress> progressMap;
    private Map<Long, LessonLoadingState> lessonIdToLoadingStateMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication
                .component()
                .plus(new UnitsModule())
                .inject(this);

        setRetainInstance(true);
        setHasOptionsMenu(true);
        section = getArguments().getParcelable(SECTION_KEY);
        lessonIdToLoadingStateMap = new HashMap<>();
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
        adapter = new UnitAdapter(section, unitList, lessonList, progressMap, (AppCompatActivity) getActivity(), lessonIdToLoadingStateMap);
        unitsRecyclerView.setAdapter(adapter);
        unitsRecyclerView.setItemAnimator(new SlideInRightAnimator());
        unitsRecyclerView.getItemAnimator().setRemoveDuration(ANIMATION_DURATION);
        unitsRecyclerView.getItemAnimator().setAddDuration(ANIMATION_DURATION);
        unitsRecyclerView.getItemAnimator().setMoveDuration(ANIMATION_DURATION);


        ProgressHelper.activate(progressBar);

        bus.register(this);
        downloadingProgressUnitsPresenter.attachView(this);
        unitsPresenter.attachView(this);
        unitsPresenter.showUnits(section, false);
    }

    @Override
    public void onDestroyView() {
        unitsPresenter.detachView(this);
        downloadingProgressUnitsPresenter.detachView(this);
        bus.unregister(this);

        lessonManager.reset();

        super.onDestroyView();
    }

    @Override
    public void onStop() {
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

                int position = shell.getSharedPreferenceHelper().getTempPosition();
                if (adapter != null) {
                    adapter.requestClickLoad(position);
                }
            }
        }
    }

    private void updateState(long unitId, boolean isCached, boolean isLoading) {
        int position = -1;
        Unit unit = null;
        for (int i = 0; i < unitList.size(); i++) {
            if (unitList.get(i).getId() == unitId) {
                position = i;
                unit = unitList.get(i);
                break;
            }
        }
        if (unit == null || position == -1 || position >= unitList.size()) return;

        unit.set_cached(isCached);
        unit.set_loading(isLoading);
        adapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onNotCachedSection(NotCachedUnitEvent e) {
        long unitId = e.getUnitId();
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
    public void onUnitScoreChanged(UnitScoreUpdateEvent event) {
        long unitId = event.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;

        int position = unitPairPosition.second;

        Progress progress = progressMap.get(unitId);
        if (progress != null) {
            progress.setScore(event.getNewScore() + "");
        }

        adapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onUnitCachedEvent(UnitCachedEvent e) {
        long unitId = e.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        //now we have not null unit and correct position at oldList
        unit.set_cached(true);
        unit.set_loading(false);
        adapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onUnitProgressStateChanged(UnitProgressUpdateEvent event) {
        long unitId = event.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        unit.set_viewed_custom(true);
        adapter.notifyItemChanged(position);
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

        Timber.d("notify position = %s", position);
        adapter.notifyItemChanged(position);
    }
}
