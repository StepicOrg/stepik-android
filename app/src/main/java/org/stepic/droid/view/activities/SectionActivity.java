package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.concurrency.FromDbSectionTask;
import org.stepic.droid.concurrency.ToDbSectionTask;
import org.stepic.droid.events.sections.FailureResponseSectionEvent;
import org.stepic.droid.events.sections.FinishingGetSectionFromDbEvent;
import org.stepic.droid.events.sections.FinishingSaveSectionToDbEvent;
import org.stepic.droid.events.sections.StartingSaveSectionToDbEvent;
import org.stepic.droid.events.sections.SuccessResponseSectionsEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.SectionAdapter;
import org.stepic.droid.web.SectionsStepicResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class SectionActivity extends FragmentActivityBase implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "enrolledActivity";

    @Bind(R.id.swipe_refresh_layout_units)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.sections_recycler_view)
    RecyclerView mSectionsRecyclerView;

    @Bind(R.id.load_progressbar)
    ProgressBar mProgressBar;

    @Bind(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;

    private Course mCourse;
    private SectionAdapter mAdapter;
    private List<Section> mSectionList;
    private FromDbSectionTask mFromDbSectionTask;
    private ToDbSectionTask mToDbSectionTask;


    boolean isScreenEmpty;
    boolean firstLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
        hideSoftKeypad();
        isScreenEmpty = true;
        firstLoad = true;

        mCourse = (Course) (getIntent().getExtras().get(AppConstants.KEY_COURSE_BUNDLE));

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSectionList = new ArrayList<>();
        mAdapter = new SectionAdapter(mSectionList, this);
        mSectionsRecyclerView.setAdapter(mAdapter);

        ProgressHelper.activate(mProgressBar);
        getAndShowSectionsFromCache();

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

    private void updateSections() {
        mShell.getApi().getSections(mCourse.getSections()).enqueue(new Callback<SectionsStepicResponse>() {
            @Override
            public void onResponse(Response<SectionsStepicResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SuccessResponseSectionsEvent(mCourse, response, retrofit));
                } else {
                    bus.post(new FailureResponseSectionEvent(mCourse));
                }

            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailureResponseSectionEvent(mCourse));
            }
        });
    }

    private void getAndShowSectionsFromCache() {
        mFromDbSectionTask = new FromDbSectionTask(mCourse);
        mFromDbSectionTask.execute();
    }

    private void showSections(List<Section> sections) {
        mSectionList.clear();
        mSectionList.addAll(sections);
        mAdapter.notifyDataSetChanged();
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
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    @Override
    public void onRefresh() {
        updateSections();
    }


    private void saveDataToCache(List<Section> sections) {
        mToDbSectionTask = new ToDbSectionTask(sections);
        mToDbSectionTask.execute();
    }

    @Subscribe
    public void onFailureDownload(FailureResponseSectionEvent e) {
        if (mCourse.getCourseId() == e.getCourse().getCourseId()) {
            ProgressHelper.dismiss(mSwipeRefreshLayout);
        }
    }

    @Subscribe
    public void onGettingFromDb(FinishingGetSectionFromDbEvent event) {
        if (event.getCourse().getCourseId() != mCourse.getCourseId()) return;

        List<Section> sections = event.getSectionList();

        if (sections != null & sections.size() != 0) {
            showSections(sections);
            if (firstLoad) {
                firstLoad = false;
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        ProgressHelper.activate(mSwipeRefreshLayout);
                        updateSections();
                    }
                });
            }
        } else {
            updateSections();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        ProgressHelper.dismiss(mSwipeRefreshLayout);
    }

    @Subscribe
    public void onSuccessDownload(SuccessResponseSectionsEvent e) {
        if (mCourse.getCourseId() == e.getCourse().getCourseId()) {
            SectionsStepicResponse stepicResponse = e.getResponse().body();
            List<Section> sections = stepicResponse.getSections();
            saveDataToCache(sections);
        }
    }

    @Subscribe
    public void onStartSaveToDb(StartingSaveSectionToDbEvent e) {
    }

    @Subscribe
    public void onFinishSaveToDb(FinishingSaveSectionToDbEvent e) {
        getAndShowSectionsFromCache();
    }
}
