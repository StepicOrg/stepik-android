package org.stepic.droid.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.otto.Bus;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.ILessonSessionManager;
import org.stepic.droid.core.ILoginManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.notifications.INotificationManager;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.IStepResolver;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.Unbinder;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class FragmentActivityBase extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    protected Unbinder unbinder;

    @Inject
    protected IConfig mConfig;

    @Inject
    protected Analytic analytic;

    @Inject
    protected INotificationManager notificationManager;

    @Inject
    protected SharedPreferenceHelper mSharedPreferenceHelper;

    @Inject
    protected ILessonSessionManager mLessonManager;

    @Inject
    protected CoursePropertyResolver mCoursePropertyResolver;

    @Inject
    protected DatabaseFacade mDbManager;

    @Inject
    public IMainHandler mMainHandler;

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

    @Inject
    protected ThreadPoolExecutor mThreadPoolExecutor;

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
        if( unbinder!=null) {
            unbinder.unbind();
        }
    }

    protected void setFragment(@IdRes int res, Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(res, fragment, fragment.getClass().toString());
        fragmentTransaction.commit();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    protected boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                return false;
            }
            return false;
        }
        return true;
    }
}
