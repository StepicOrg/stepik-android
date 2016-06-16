package org.stepic.droid.view.activities;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.stepic.droid.base.SingleFragmentActivity;
import org.stepic.droid.model.Course;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.fragments.CourseDetailFragment;

public class CourseDetailActivity extends SingleFragmentActivity {
    @NonNull
    @Override
    protected Fragment createFragment() {
        Course course = (Course) (getIntent().getExtras().get(AppConstants.KEY_COURSE_BUNDLE));
        if (course == null) {
            int i = 0;
            //Warning: work only for pattern android:pathPattern="/course/.*/" NOT Working for /course/.*/.* !!!
            String pathFromWeb = getIntent().getData().getLastPathSegment(); //example of last path segment: Школьная-физика-Тепловые-и-электромагнитные-явления-432/
            return CourseDetailFragment.newInstance(course); //// FIXME: 16.06.16 send id
        } else {
            return CourseDetailFragment.newInstance(course);
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
}
