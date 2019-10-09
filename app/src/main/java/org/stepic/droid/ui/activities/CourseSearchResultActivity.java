package org.stepic.droid.ui.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.MenuItem;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.ui.fragments.CourseSearchFragment;

public class CourseSearchResultActivity extends FragmentActivityBase {

    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.search_title);
        setContentView(R.layout.activity_search_courses);
        query = getIntent().getStringExtra(SearchManager.QUERY);
        initOrTryRestoreFragment();
    }

    private void initOrTryRestoreFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frame);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.frame, fragment)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //add new fragment
        setIntent(intent);
        query = intent.getStringExtra(SearchManager.QUERY);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = createFragment();
        fm.beginTransaction()
                .replace(R.id.frame, fragment)
                .commit();
    }

    private Fragment createFragment() {
        return CourseSearchFragment.Companion.newInstance(query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

}
