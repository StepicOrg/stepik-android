package org.stepic.droid.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import org.stepic.droid.core.DefaultFilter;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.ILoginManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.notifications.INotificationManager;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.ui.fragments.MyCoursesFragment;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.Unbinder;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class FragmentActivityBase extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    protected Unbinder unbinder;

    @Inject
    protected TextResolver textResolver;

    @Inject
    protected ShareHelper shareHelper;

    @Inject
    protected DefaultFilter defaultFilter;

    @Inject
    protected IConfig config;

    @Inject
    protected Analytic analytic;

    @Inject
    protected INotificationManager notificationManager;

    @Inject
    protected SharedPreferenceHelper sharedPreferenceHelper;

    @Inject
    protected LessonSessionManager lessonManager;

    @Inject
    protected CoursePropertyResolver coursePropertyResolver;

    @Inject
    protected DatabaseFacade databaseFacade;

    @Inject
    public IMainHandler mainHandler;

    @Inject
    protected IShell shell;

    @Inject
    protected Bus bus;

    @Inject
    protected UserPreferences userPreferences;

    @Inject
    protected ILoginManager loginManager;

    @Inject
    protected ThreadPoolExecutor threadPoolExecutor;

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


    public void applyTransitionPrev() {
        // apply slide transition animation
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    protected void setFragment(@IdRes int res, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(res, fragment, fragment.getClass().getSimpleName());
        int countInBackStack = fragmentManager.getBackStackEntryCount();
        boolean isRootScreen = MyCoursesFragment.class.getSimpleName().equals(fragment.getClass().getSimpleName());
        if ((isRootScreen && countInBackStack < 1) || (!isRootScreen && countInBackStack < 2)) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
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
                //do not show Google Services dialog
                analytic.reportEvent(Analytic.Error.GOOGLE_SERVICES_TOO_OLD); //it is resolvable, but we do not want push user for updating services
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                return false;
            }
            return false;
        }
        return true;
    }
}
