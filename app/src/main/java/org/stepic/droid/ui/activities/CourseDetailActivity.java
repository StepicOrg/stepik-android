package org.stepic.droid.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.SingleFragmentActivity;
import org.stepic.droid.model.Course;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.ui.fragments.CourseDetailFragment;

import java.util.List;

public class CourseDetailActivity extends SingleFragmentActivity {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
        Bundle extras = getIntent().getExtras();
        Course course = null;
        if (extras != null) {
            course = (Course) (extras.get(AppConstants.KEY_COURSE_BUNDLE));
        }
        if (course == null) {
            //Warning: work only for pattern android:pathPattern="/course/.*/" NOT Working for /course/.*/.* !!!
            Intent intent = getIntent();
            Uri dataUri = intent.getData();
            String pathFromWeb = dataUri.getLastPathSegment();//example of last path segment: Школьная-физика-Тепловые-и-электромагнитные-явления-432
            if (pathFromWeb.equals(AppConstants.APP_INDEXING_COURSE_DETAIL_MANIFEST_HACK)) {
                List<String> pathSegments = dataUri.getPathSegments();
                if (pathSegments.size() - 2 >= 0) {
                    pathFromWeb = pathSegments.get(pathSegments.size() - 2); //try hack android system, which cut "/" in path
                }
            }
            Long id = HtmlHelper.parseIdFromSlug(pathFromWeb);
            long simpleId;
            if (id == null) {
                simpleId = -1;
            } else {
                simpleId = id;
            }
            analytic.reportEvent(Analytic.DeepLink.USER_OPEN_COURSE_DETAIL_LINK, simpleId+"");
            return CourseDetailFragment.newInstance(simpleId);
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
