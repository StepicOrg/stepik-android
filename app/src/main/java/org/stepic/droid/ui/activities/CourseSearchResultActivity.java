package org.stepic.droid.ui.activities;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.ui.fragments.CourseSearchFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CourseSearchResultActivity extends FragmentActivityBase {

    private final static String TAG = "SearchActivity";

    @BindView(R.id.frame)
    View rootFrame;

    private MenuItem menuItem;
    private SearchView searchView;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.search_title);
        setContentView(R.layout.activity_search_courses);
        unbinder = ButterKnife.bind(this);
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
        return CourseSearchFragment.newInstance(query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();

        ImageView closeImageView = searchView.findViewById(R.id.search_close_btn);
        closeImageView.setImageDrawable(ContextCompat.getDrawable(this, getCloseIconDrawableRes()));

        ImageView searchButtonImageView = searchView.findViewById(R.id.search_button);
        searchButtonImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_comment_black_24dp));

        ComponentName componentName = getComponentName();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        searchView.setSearchableInfo(searchableInfo);
        searchView.setMaxWidth(20000);//it is dirty hack for expand in landscape
        menuItem.expandActionView();
        if (query != null) {
            searchView.setQuery(query, false);
        }
        searchView.clearFocus();

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return true;
            }
        });
        return true;
    }

    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

}
