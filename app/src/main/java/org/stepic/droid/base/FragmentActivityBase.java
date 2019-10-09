package org.stepic.droid.base;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.services.NotificationsViewPusher;
import org.stepic.droid.ui.util.CloseIconHolder;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.resolvers.text.TextResolver;
import org.stepik.android.model.Course;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

public abstract class FragmentActivityBase extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String screenshotServiceName = "com.android.systemui:screenshot";

    @Inject
    protected TextResolver textResolver;

    @Inject
    protected ShareHelper shareHelper;

    @Inject
    protected Config config;

    @Inject
    protected Analytic analytic;

    @Inject
    protected SharedPreferenceHelper sharedPreferenceHelper;

    @Inject
    protected ScreenManager screenManager;

    @Inject
    protected NotificationsViewPusher notificationsViewPusher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.Companion.component().inject(this);

        if (savedInstanceState == null && AppConstants.OPEN_NOTIFICATION.equals(getIntent().getAction())) {
            notificationsViewPusher.pushToViewedNotificationsQueue(
                    getIntent().getLongExtra(AppConstants.KEY_NOTIFICATION_ID, 0));
        }
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


    protected boolean wasLaunchedFromRecents() {
        return (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;
    }


    @Nullable
    protected Course getCourseFromExtra() {
        return getIntent().getParcelableExtra(AppConstants.KEY_COURSE_BUNDLE);
    }


    @Override
    protected void onStart() {
        super.onStart();
//        detectScreenShotService();
    }

    @Override
    protected void onStop() {
//        stopDetectingOfScreenshots();
        super.onStop();
    }

    private Handler detectScreenshotHandler;
    ScreenshotDetectionRunnable screenshotDetectionRunnable;
    static final int delay = 2000;

    private void stopDetectingOfScreenshots() {
        detectScreenshotHandler.removeCallbacks(screenshotDetectionRunnable);
    }

    private void detectScreenShotService() {
        HandlerThread handlerThread = new HandlerThread("detect_thread");
        handlerThread.start();
        detectScreenshotHandler = new Handler(handlerThread.getLooper());
        screenshotDetectionRunnable = new ScreenshotDetectionRunnable(this, detectScreenshotHandler);
        detectScreenshotHandler.postDelayed(screenshotDetectionRunnable, delay);
    }

    static class ScreenshotDetectionRunnable implements Runnable {
        WeakReference<FragmentActivityBase> activityWeakReference;
        private final Handler detectScreenShotHandler;

        ScreenshotDetectionRunnable(FragmentActivityBase activity, Handler handler) {
            activityWeakReference = new WeakReference<>(activity);
            this.detectScreenShotHandler = handler;
        }

        @Override
        public void run() {
            FragmentActivityBase activity = activityWeakReference.get();
            if (activity != null) {
                final ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningServiceInfo> rs = activityManager.getRunningServices(200);
                for (ActivityManager.RunningServiceInfo ar : rs) {
                    if (ar.process.equals(screenshotServiceName)) {
                        activity.analytic.reportEventWithName(Analytic.Interaction.SCREENSHOT, activity.getClass().getSimpleName());
                    }
                }
                detectScreenShotHandler.postDelayed(this, delay);
            }

        }
    }

    @DrawableRes
    protected final int getCloseIconDrawableRes() {
        return CloseIconHolder.INSTANCE.getCloseIconDrawableRes();
    }
}
