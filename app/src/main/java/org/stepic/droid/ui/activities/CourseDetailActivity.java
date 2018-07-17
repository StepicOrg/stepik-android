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
import org.stepik.android.model.structure.Course;
import org.stepic.droid.ui.fragments.CourseDetailFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.HtmlHelper;

import java.util.List;

public class CourseDetailActivity extends SingleFragmentActivity {

    public static final String INSTA_ENROLL_KEY = "insta_enroll";

    @NonNull
    @Override
    protected Fragment createFragment() {
        Bundle extras = getIntent().getExtras();
        Course course = null;
        boolean needInstaEnroll = false;
        if (extras != null) {
            course = extras.getParcelable(AppConstants.KEY_COURSE_BUNDLE);
            needInstaEnroll = extras.getBoolean(INSTA_ENROLL_KEY);
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
            analytic.reportEvent(Analytic.DeepLink.USER_OPEN_COURSE_DETAIL_LINK, simpleId + "");
            analytic.reportEvent(Analytic.DeepLink.USER_OPEN_LINK_GENERAL);
            return CourseDetailFragment.newInstance(simpleId);
        } else {
            return CourseDetailFragment.newInstance(course, needInstaEnroll);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.course_info_title);
    }

    @Override
    public void applyTransitionPrev() {
        //no-op
    }
}
