package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.events.steps.FailLoadStepEvent;
import org.stepic.droid.events.steps.SuccessLoadStepEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.StepFragmentAdapter;
import org.stepic.droid.web.StepResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class StepsActivity extends FragmentActivityBase {

    public static String POSITION = "POSITION";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.viewpager)
    ViewPager mViewPager;

    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    @Bind(R.id.load_steps)
    ProgressBar mProgressBar;

    @BindString(R.string.not_available_lesson)
    String notAvailable;


    StepFragmentAdapter mStepAdapter;
    private List<Step> mStepList;
    private Unit mUnit;
    private Lesson mLesson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        ButterKnife.bind(this);

        mUnit = (Unit) (getIntent().getExtras().get(AppConstants.KEY_UNIT_BUNDLE));
        mLesson = (Lesson) (getIntent().getExtras().get(AppConstants.KEY_LESSON_BUNDLE));
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(mLesson.getTitle());
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //may be set title == title of lesson?

        mStepList = new ArrayList<>();
        mStepAdapter = new StepFragmentAdapter(getSupportFragmentManager(), this, mStepList);
        mViewPager.setAdapter(mStepAdapter);

        if (mLesson != null && mLesson.getSteps() != null && mLesson.getSteps().length != 0)
            updateStates();
    }


    private void updateStates() {
        ProgressHelper.activate(mProgressBar);
        mShell.getApi().getSteps(mLesson.getSteps()).enqueue(new Callback<StepResponse>() {
            @Override
            public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SuccessLoadStepEvent(response));
                } else {
                    bus.post(new FailLoadStepEvent());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailLoadStepEvent());
            }
        });
    }

    @Subscribe
    public void onSuccessLoad(SuccessLoadStepEvent e) {
        //// FIXME: 10.10.15 check right lesson ?? is it need?
        StepResponse stepicResponse = e.getResponse().body();
        List<Step> steps = stepicResponse.getSteps();

        if (steps.isEmpty()) {
            bus.post(new FailLoadStepEvent());
        } else {
            mStepList.clear();
            mStepList.addAll(steps);
            mStepAdapter.notifyDataSetChanged();
            updateTabs();
            ProgressHelper.dismiss(mProgressBar);
        }
    }

    @Subscribe
    public void onFailLoad(FailLoadStepEvent e) {
        Toast.makeText(this, notAvailable, Toast.LENGTH_LONG).show();
        ProgressHelper.dismiss(mProgressBar);
    }


    private void updateTabs() {
        mTabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < mStepAdapter.getCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(mStepAdapter.getTabView(i));
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
}
