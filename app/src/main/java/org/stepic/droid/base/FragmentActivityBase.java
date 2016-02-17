package org.stepic.droid.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.squareup.otto.Bus;

import org.stepic.droid.R;
import org.stepic.droid.core.ILessonSessionManager;
import org.stepic.droid.core.ILoginManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.IStepResolver;

import javax.inject.Inject;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class FragmentActivityBase extends AppCompatActivity {

    @Inject
    protected ILessonSessionManager mLessonManager;

    @Inject
    protected CoursePropertyResolver mCoursePropertyResolver;

    @Inject
    protected DatabaseFacade mDbManager;

    @Inject
    protected IShell mShell;

    @Inject
    protected Bus bus;

    @Inject
    protected UserPreferences mUserPreferences;
    @Inject
    protected IStepResolver mStepResolver;

    @Inject
    protected ILoginManager mLoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.component(this).inject(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void hideSoftKeypad() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // A method to find height of the status bar
    protected int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void finish() {
        super.finish();
        applyTransitionPrev();
    }


    private void applyTransitionPrev() {
        // apply slide transition animation
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    protected void setFragment(@IdRes int res, Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(res, fragment);
        fragmentTransaction.commit();
    }
}
