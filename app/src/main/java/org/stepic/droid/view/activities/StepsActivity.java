package org.stepic.droid.view.activities;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.base.SingleFragmentActivity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.fragments.StepsFragment;

public class StepsActivity extends SingleFragmentActivity {


    @NotNull
    @Override
    protected Fragment createFragment() {
        Unit unit = (Unit) (getIntent().getExtras().get(AppConstants.KEY_UNIT_BUNDLE));
        Lesson lesson = (Lesson) (getIntent().getExtras().get(AppConstants.KEY_LESSON_BUNDLE));
        return StepsFragment.newInstance(unit, lesson);
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
}
