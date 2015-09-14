package com.elpatika.stepic.view.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.StepicBaseFragment;
import com.elpatika.stepic.base.StepicBaseFragmentActivity;
import com.elpatika.stepic.core.IShell;
import com.elpatika.stepic.util.SharedPreferenceHelper;
import com.elpatika.stepic.view.fragments.BestLessons;
import com.elpatika.stepic.view.fragments.MyCourses;
import com.elpatika.stepic.web.AuthenticationStepicResponse;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class MainFeedActivity extends StepicBaseFragmentActivity {
    @Inject
    IShell mShell;


    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);
        ButterKnife.bind(this);


        setTitle(R.string.my_courses_title);
//        SharedPreferenceHelper sharedPreferenceHelper = mShell.getSharedPreferenceHelper();
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper();
        AuthenticationStepicResponse resp = sharedPreferenceHelper.getAuthResponseFromStore(MainFeedActivity.this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //Initializing NavigationView
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {




                //Closing drawer on item click
                mDrawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {


                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.first:
                        setFragment(new MyCourses());
                        return true;

                    // For rest of the options we just show a toast on click

                    case R.id.second:
                        setFragment(new BestLessons());
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,R.string.drawer_open, R.string.drawer_closed){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();


    }


    private void setFragment (StepicBaseFragment fragment) {
        Toast.makeText(getApplicationContext(), "we change fragment", Toast.LENGTH_SHORT).show();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment);
        fragmentTransaction.commit();
    }
//
//
//    @Override
//    public Map<Key<?>, Object> getScopedObjectMap() {
//        return null;
//    }
}
