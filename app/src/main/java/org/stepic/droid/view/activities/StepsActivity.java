package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StepsActivity extends FragmentActivityBase {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.viewpager)
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //may be set title == title of lesson?


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
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }
}
