package org.stepic.droid.view.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchCourseResultActivity extends FragmentActivityBase {

    @Bind(R.id.swipe_refresh_layout_mycourses)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.list_of_courses)
    protected ListView mListOfCourses;

    @Bind(R.id.report_problem)
    protected View mReportConnectionProblem;

    @Bind(R.id.empty_courses)
    protected View mEmptyCoursesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_courses);
        ButterKnife.bind(this);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        //just the stub
        mListOfCourses.setVisibility(View.GONE);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        mEmptyCoursesView.setVisibility(View.VISIBLE);
        mReportConnectionProblem.setVisibility(View.GONE);
    }
}
