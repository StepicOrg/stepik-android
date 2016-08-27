package org.stepic.droid.ui.activities;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.base.SingleFragmentActivity;
import org.stepic.droid.ui.fragments.FilterFragment;

public class FilterActivity extends SingleFragmentActivity {
    @Nullable
    @Override
    protected Fragment createFragment() {
        return FilterFragment.newInstance();
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
