package org.stepic.droid.view.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.concurrency.FromDbUnitLessonTask;
import org.stepic.droid.concurrency.ToDbUnitLessonTask;
import org.stepic.droid.events.lessons.SuccessLoadLessonsEvent;
import org.stepic.droid.events.notify_ui.NotifyUIUnitLessonEvent;
import org.stepic.droid.events.units.FailureLoadEvent;
import org.stepic.droid.events.units.LoadedFromDbUnitsLessonsEvent;
import org.stepic.droid.events.units.SuccessLoadUnitsEvent;
import org.stepic.droid.events.units.UnitLessonSavedEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.view.adapters.UnitAdapter;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.UnitStepicResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class UnitsActivity extends FragmentActivityBase implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.swipe_refresh_layout_units)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.units_recycler_view)
    RecyclerView mUnitsRecyclerView;

    @Bind(R.id.load_progressbar)
    ProgressBar mProgressBar;

    @Bind(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;


    private Section mSection;
    private UnitAdapter mAdapter;
    private List<Unit> mUnitList;
    private List<Lesson> mLessonList;

    private FromDbUnitLessonTask mFromDbTask;
    private ToDbUnitLessonTask mToDbTask;
    private Handler mHandlerStateUpdating;
    private Runnable mUpdatingRunnable;

    boolean isScreenEmpty;
    boolean firstLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);
        ButterKnife.bind(this);
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
        mAdapter = new UnitAdapter(this, mSection, mUnitList, mLessonList);
        mUnitsRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        ProgressHelper.activate(mProgressBar);
        getAndShowUnitsFromCache();
    }

    ArrayList<Unit> localCopy;

    private void updateState() {
        if (localCopy == null) {
            localCopy = new ArrayList<>(mUnitList);
        }
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (localCopy == null || mAdapter == null || localCopy.size() == 0) {
                    return null;
                }
                for (Unit unit : localCopy) {
                    unit.setIs_cached(mDbManager.isUnitCached(unit));
                    unit.setIs_loading(mDbManager.isUnitLoading(unit));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                bus.post(new NotifyUIUnitLessonEvent());
            }
        };
        task.execute();
    }

    private void getAndShowUnitsFromCache() {
        mFromDbTask = new FromDbUnitLessonTask(mSection);
        mFromDbTask.execute();
    }


    private void updateUnits() {
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


    @Subscribe
    public void onSuccessLoadUnits(SuccessLoadUnitsEvent e) {
        if (mSection == null || e.getmSection() == null
                || e.getmSection().getId() != mSection.getId())
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
    public void onFinalSuccessDownloadFromWeb(SuccessLoadLessonsEvent e) {
        if (mSection == null || e.getSection() == null
                || e.getSection().getId() != mSection.getId())
            return;

        saveToDb(e.getUnits(), e.getResponse().body().getLessons());
    }

    private void saveToDb(List<Unit> unitList, List<Lesson> lessonList) {
        mToDbTask = new ToDbUnitLessonTask(mSection, unitList, lessonList);
        mToDbTask.execute();
    }

    private void showUnitsLessons(List<Unit> units, List<Lesson> lessons) {

        mLessonList.clear();
        mLessonList.addAll(lessons);

        mUnitList.clear();
        mUnitList.addAll(units);
        mAdapter.notifyDataSetChanged();

        dismiss();
    }


    @Subscribe
    public void onFailLoad(FailureLoadEvent e) {
        if (mSection == null || e.getmSection() == null
                || e.getmSection().getId() != mSection.getId())
            return;
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mHandlerStateUpdating = new Handler();
        mUpdatingRunnable = new Runnable() {
            @Override
            public void run() {
                updateState();
            }
        };
        mHandlerStateUpdating.post(mUpdatingRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        mHandlerStateUpdating.removeCallbacks(mUpdatingRunnable);
    }


    @Override
    public void onRefresh() {
        ProgressHelper.activate(mSwipeRefreshLayout);
        updateUnits();
    }

    @Subscribe
    public void onSuccessLoadFromDb(LoadedFromDbUnitsLessonsEvent e) {
        if (mSection != e.getSection()) return;
        if (e.getUnits() != null && e.getLessons() != null && e.getUnits().size() != 0 && e.getLessons().size() != 0) {
            showUnitsLessons(e.getUnits(), e.getLessons());
            if (firstLoad) {
                firstLoad = false;
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        ProgressHelper.activate(mSwipeRefreshLayout);
                        updateUnits();
                    }
                });
            }
        } else {
            //db doesn't have it, load from web with empty screen
            updateUnits();
        }
    }

    @Subscribe
    public void onFinishSaveToDb(UnitLessonSavedEvent e) {
        if (e.getmSection() == mSection) {
            getAndShowUnitsFromCache();
        }
    }


    @Subscribe
    public void onNotifyUI(NotifyUIUnitLessonEvent event) {
        mAdapter.notifyDataSetChanged();
        mHandlerStateUpdating.postDelayed(mUpdatingRunnable, AppConstants.UI_UPDATING_TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
