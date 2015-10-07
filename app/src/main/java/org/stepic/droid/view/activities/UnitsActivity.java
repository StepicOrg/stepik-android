package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.events.units.FailureLoadUnitsEvent;
import org.stepic.droid.events.units.SuccessLoadUnitsEvent;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.UnitAdapter;
import org.stepic.droid.view.decorators.DividerItemDecoration;
import org.stepic.droid.web.UnitStepicResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class UnitsActivity extends StepicBaseFragmentActivity {
    @Bind(R.id.units_recycler_view)
    RecyclerView mUnitsRecyclerView;

    @Bind(R.id.load_sections)
    ProgressBar mProgressBar;

    @Bind(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;


    private Section mSection;
    private UnitAdapter mAdapter;
    private List<Unit> mUnitList;

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
        mAdapter = new UnitAdapter(this, mSection, mUnitList);
        mUnitsRecyclerView.setAdapter(mAdapter);
        mUnitsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));


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
                    bus.post(new FailureLoadUnitsEvent(mSection));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailureLoadUnitsEvent(mSection));
            }
        });

    }


    @Subscribe
    public void onSuccessDownload(SuccessLoadUnitsEvent e) {
        if (mSection == null || e.getmSection() == null
                || e.getmSection().getId() != mSection.getId())
            return;

        UnitStepicResponse unitStepicResponse = e.getResponse().body();
        List<Unit>  units = unitStepicResponse.getUnits();

        mUnitList.clear();
        mUnitList.addAll(units);
        mAdapter.notifyDataSetChanged();
        ProgressHelper.dismiss(mProgressBar);
    }

    @Subscribe
    public void onFailLoad (FailureLoadUnitsEvent e) {
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
}
