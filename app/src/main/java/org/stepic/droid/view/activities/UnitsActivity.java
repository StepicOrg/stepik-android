package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.model.Section;
import org.stepic.droid.util.AppConstants;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UnitsActivity extends StepicBaseFragmentActivity {
    @Bind(R.id.units_recycler_view)
    RecyclerView mSectionsRecyclerView;

    @Bind(R.id.load_sections)
    ProgressBar mProgressBar;

    @Bind(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;


    Section mSection;

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
