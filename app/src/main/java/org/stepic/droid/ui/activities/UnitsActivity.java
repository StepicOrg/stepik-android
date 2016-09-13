package org.stepic.droid.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.concurrency.tasks.FromDbUnitLessonTask;
import org.stepic.droid.concurrency.tasks.ToDbUnitLessonTask;
import org.stepic.droid.events.lessons.SuccessLoadLessonsEvent;
import org.stepic.droid.events.notify_ui.NotifyUIUnitLessonEvent;
import org.stepic.droid.events.units.FailureLoadEvent;
import org.stepic.droid.events.units.LoadedFromDbUnitsLessonsEvent;
import org.stepic.droid.events.units.NotCachedUnitEvent;
import org.stepic.droid.events.units.SuccessLoadUnitsEvent;
import org.stepic.droid.events.units.UnitCachedEvent;
import org.stepic.droid.events.units.UnitLessonSavedEvent;
import org.stepic.droid.events.units.UnitProgressUpdateEvent;
import org.stepic.droid.events.units.UnitScoreUpdateEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.ui.adapters.UnitAdapter;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.ProgressUtil;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.ProgressesResponse;
import org.stepic.droid.web.UnitStepicResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class UnitsActivity extends FragmentActivityBase implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.swipe_refresh_layout_units)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.units_recycler_view)
    RecyclerView mUnitsRecyclerView;

    @BindView(R.id.load_progressbar)
    ProgressBar mProgressBar;

    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;

    @BindView(R.id.report_problem)
    protected View mReportConnectionProblem;

    @BindView(R.id.report_empty)
    protected View mReportEmpty;


    private Section mSection;
    private UnitAdapter mAdapter;
    private List<Unit> mUnitList;
    private List<Lesson> mLessonList;
    private Map<Long, Progress> mUnitProgressMap;

    private FromDbUnitLessonTask mFromDbTask;
    private ToDbUnitLessonTask mToDbTask;

    boolean isScreenEmpty;
    boolean firstLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);
        unbinder = ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
        hideSoftKeypad();
        isScreenEmpty = true;
        firstLoad = true;
        mSection = (Section) (getIntent().getExtras().get(AppConstants.KEY_SECTION_BUNDLE));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUnitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUnitList = new ArrayList<>();
        mLessonList = new ArrayList<>();
        mUnitProgressMap = new HashMap<>();
        mAdapter = new UnitAdapter(mSection, mUnitList, mLessonList, mUnitProgressMap, this);
        mUnitsRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        ProgressHelper.activate(mProgressBar);

        bus.register(this);
        getAndShowUnitsFromCache();
    }

    private void getAndShowUnitsFromCache() {
        mFromDbTask = new FromDbUnitLessonTask(mSection);
        mFromDbTask.executeOnExecutor(mThreadPoolExecutor);
    }


    private void updateUnits() {
        long[] units = mSection.getUnits();
        if (units == null || units.length == 0) {
            ProgressHelper.dismiss(mProgressBar);
            ProgressHelper.dismiss(mSwipeRefreshLayout);
            mReportEmpty.setVisibility(View.VISIBLE);
        } else {
            mReportEmpty.setVisibility(View.GONE);
            mShell.getApi().getUnits(mSection.getUnits()).enqueue(new Callback<UnitStepicResponse>() {
                @Override
                public void onResponse(Response<UnitStepicResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        bus.post(new SuccessLoadUnitsEvent(mSection, response, retrofit));
                    } else {
                        bus.post(new FailureLoadEvent(mSection));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new FailureLoadEvent(mSection));
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_menu, menu);
        return true;
    }

    @Subscribe
    public void onSuccessLoadUnits(SuccessLoadUnitsEvent e) {
        if (mSection == null || e.getSection() == null
                || e.getSection().getId() != mSection.getId())
            return;

        UnitStepicResponse unitStepicResponse = e.getResponse().body();
        final List<Unit> units = unitStepicResponse.getUnits();

        long[] lessonsIds = StepicLogicHelper.fromUnitsToLessonIds(units);
        mShell.getApi().getLessons(lessonsIds).enqueue(new Callback<LessonStepicResponse>() {
            @Override
            public void onResponse(Response<LessonStepicResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SuccessLoadLessonsEvent(mSection, response, retrofit, units));
                } else {
                    bus.post(new FailureLoadEvent(mSection));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailureLoadEvent(mSection));
            }
        });
    }

    @Subscribe
    public void onFinalSuccessDownloadFromWeb(final SuccessLoadLessonsEvent e) {
        if (mSection == null || e.getSection() == null
                || e.getSection().getId() != mSection.getId())
            return;

        String[] progressIds = ProgressUtil.getAllProgresses(e.getUnits());

        mShell.getApi().getProgresses(progressIds).enqueue(new Callback<ProgressesResponse>() {
            List<Unit> units = e.getUnits();
            List<Lesson> lessons = e.getResponse().body().getLessons();

            public void onResponse(Response<ProgressesResponse> response, Retrofit retrofit) {

                if (response.isSuccess()) {
                    saveToDb(units, lessons, response.body().getProgresses());
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    private void saveToDb(List<Unit> unitList, List<Lesson> lessonList, List<Progress> progresses) {
        mToDbTask = new ToDbUnitLessonTask(mSection, unitList, lessonList, progresses);
        mToDbTask.executeOnExecutor(mThreadPoolExecutor);
    }

    private void showUnitsLessons(List<Unit> units, List<Lesson> lessons, Map<Long, Progress> longProgressMap) {

        mLessonList.clear();
        mLessonList.addAll(lessons);

        mUnitList.clear();
        mUnitList.addAll(units);

        mUnitProgressMap.clear();
        mUnitProgressMap.putAll(longProgressMap);

        dismissReport();
        mAdapter.notifyDataSetChanged();

        dismiss();
    }


    @Subscribe
    public void onFailLoad(FailureLoadEvent e) {
        if (mSection == null || e.getSection() == null
                || e.getSection().getId() != mSection.getId())
            return;

        if (mUnitList != null && mUnitList.size() == 0) {
            mReportConnectionProblem.setVisibility(View.VISIBLE);
        }
        dismiss();
    }

    private void dismiss() {
        if (isScreenEmpty) {
            ProgressHelper.dismiss(mProgressBar);
            isScreenEmpty = false;
        } else {
            ProgressHelper.dismiss(mSwipeRefreshLayout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_item_share:
                shareSection();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareSection() {
        if (mSection != null) {
            Intent intent = shareHelper.getIntentForSectionSharing(mSection);
            startActivity(intent);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ProgressHelper.dismiss(mSwipeRefreshLayout);
    }


    @Override
    public void onRefresh() {
        analytic.reportEvent(Analytic.Interaction.REFRESH_UNIT);
        ProgressHelper.activate(mSwipeRefreshLayout);
        updateUnits();
    }

    private void dismissReport() {
        if (mLessonList != null && mUnitList != null && mLessonList.size() != 0 && mUnitList.size() != 0) {
            mReportConnectionProblem.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onSuccessLoadFromDb(LoadedFromDbUnitsLessonsEvent e) {
        if (mSection != e.getSection()) return;
        if (e.getUnits() != null && e.getLessons() != null && e.getUnits().size() != 0 && e.getLessons().size() != 0) {
            showUnitsLessons(e.getUnits(), e.getLessons(), e.getProgressMap());
            if (firstLoad) {
                firstLoad = false;
                updateUnits();
            }
        } else {
            //db doesn't have it, load from web with empty screen
            updateUnits();
        }
    }

    @Subscribe
    public void onFinishSaveToDb(UnitLessonSavedEvent e) {
        if (e.getSection() == mSection) {
            getAndShowUnitsFromCache();
        }
    }

    @Subscribe
    public void onNotifyUI(NotifyUIUnitLessonEvent event) {
        dismissReport();
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onUnitCachedEvent(UnitCachedEvent e) {
        long unitId = e.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        //now we have not null unit and correct position at list
        unit.set_cached(true);
        unit.set_loading(false);
        mAdapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onUnitProgressStateChanged(UnitProgressUpdateEvent event) {
        long unitId = event.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        unit.set_viewed_custom(true);
        mAdapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onUnitScoreChanged(UnitScoreUpdateEvent event) {
        long unitId = event.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        Progress progress = mUnitProgressMap.get(unitId);
        progress.setScore(event.getNewScore() + "");

        mAdapter.notifyItemChanged(position);
    }

    @Nullable
    private Pair<Unit, Integer> getUnitOnScreenAndPositionById(long unitId) {
        int position = -1;
        Unit unit = null;
        for (int i = 0; i < mUnitList.size(); i++) {
            if (mUnitList.get(i).getId() == unitId) {
                position = i;
                unit = mUnitList.get(i);
                break;
            }
        }
        if (unit == null || position == -1 || position >= mUnitList.size()) return null;
        return new Pair<>(unit, position);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        mLessonManager.reset();
        super.onDestroy();
    }

    @Subscribe
    public void onNotCachedSection(NotCachedUnitEvent e) {
        long unitId = e.getUnitId();
        updateState(unitId, false, false);
    }

    private void updateState(long unitId, boolean isCached, boolean isLoading) {

        int position = -1;
        Unit unit = null;
        for (int i = 0; i < mUnitList.size(); i++) {
            if (mUnitList.get(i).getId() == unitId) {
                position = i;
                unit = mUnitList.get(i);
                break;
            }
        }
        if (unit == null || position == -1 || position >= mUnitList.size()) return;

        unit.set_cached(isCached);
        unit.set_loading(isLoading);
        mAdapter.notifyItemChanged(position);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                int position = mShell.getSharedPreferenceHelper().getTempPosition();
                if (mAdapter != null) {
                    mAdapter.requestClickLoad(position);
                }
            }
        }
    }

}
