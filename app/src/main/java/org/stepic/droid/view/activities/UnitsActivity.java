package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.events.lessons.SuccessLoadLessonsEvent;
import org.stepic.droid.events.units.FailureLoadEvent;
import org.stepic.droid.events.units.SuccessLoadUnitsEvent;
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

public class UnitsActivity extends FragmentActivityBase {
    @Bind(R.id.units_recycler_view)
    RecyclerView mUnitsRecyclerView;

    @Bind(R.id.load_sections)
    ProgressBar mProgressBar;

    @Bind(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;


    private Section mSection;
    private UnitAdapter mAdapter;
    private List<Unit> mUnitList;
    private List<Lesson> mLessonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
        hideSoftKeypad();

        mSection = (Section) (getIntent().getExtras().get(AppConstants.KEY_SECTION_BUNDLE));
    }

    @Override
    protected void onStart() {
        super.onStart();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUnitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUnitList = new ArrayList<>();
        mLessonList = new ArrayList<>();
        mAdapter = new UnitAdapter(this, mSection, mUnitList, mLessonList);
        mUnitsRecyclerView.setAdapter(mAdapter);

        if (mSection != null && mSection.getUnits() != null && mSection.getUnits().length != 0)
            updateUnits();

    }

    private void updateUnits() {
        ProgressHelper.activate(mProgressBar);
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
    public void onSuccessDownload(SuccessLoadUnitsEvent e) {
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
    public void onSuccessLoadLessons(SuccessLoadLessonsEvent e) {
        if (mSection == null || e.getSection() == null
                || e.getSection().getId() != mSection.getId())
            return;

        mLessonList.clear();
        mLessonList.addAll(e.getResponse().body().getLessons());

        mUnitList.clear();
        mUnitList.addAll(e.getUnits());
        mAdapter.notifyDataSetChanged();
        ProgressHelper.dismiss(mProgressBar);
    }


    @Subscribe
    public void onFailLoad(FailureLoadEvent e) {
        if (mSection == null || e.getmSection() == null
                || e.getmSection().getId() != mSection.getId())
            return;

        ProgressHelper.dismiss(mProgressBar);
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
    protected void onStop() {
        super.onStop();
        mLessonList = null;
        mUnitList = null;
    }
}
