package org.stepic.droid.view.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.view.fragments.CourseSearchFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CourseSearchResultActivity extends FragmentActivityBase {


    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_courses);
        ButterKnife.bind(this);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            initActivity(query);
        }
    }

    private void initActivity(String query) {
        Fragment fragment = new CourseSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CourseSearchFragment.QUERY_KEY, query);
        fragment.setArguments(bundle);
        setFragment(R.id.frame, fragment);
        bus.register(this);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }
}
