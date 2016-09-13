package org.stepic.droid.ui.activities;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.base.SingleFragmentActivity;
import org.stepic.droid.ui.fragments.FilterFragment;

public class FilterActivity extends SingleFragmentActivity {

    public static final String FILTER_TYPE_KEY = "filter_type_key";

    @Nullable
    @Override
    protected Fragment createFragment() {
        int filterCourseTypeCode = getIntent().getIntExtra(FILTER_TYPE_KEY, -1); // look at app constants
        return FilterFragment.newInstance(filterCourseTypeCode);
    }

    @Override
    public void applyTransitionPrev() {
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
