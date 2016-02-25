package org.stepic.droid.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.ILocalProgressManager;
import org.stepic.droid.events.steps.UpdateStepEvent;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.ViewAssignment;

import java.io.IOException;

import javax.inject.Inject;

import retrofit.Response;

public class ViewPusher extends IntentService {
    @Inject
    DownloadManager mSystemDownloadManager;
    @Inject
    UserPreferences mUserPrefs;
    @Inject
    Bus mBus;
    @Inject
    IVideoResolver mResolver;
    @Inject
    IApi mApi;
    @Inject
    DatabaseFacade mDb;
    @Inject
    IStoreStateManager mStoreStateManager;

    @Inject
    ILocalProgressManager mUnitProgressManager;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p/>
     * name Used to name the worker thread, important only for debugging.
     */
    public ViewPusher() {
        super("view_state_pusher");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainApplication.component().inject(this);
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final long stepId = intent.getLongExtra(AppConstants.KEY_STEP_BUNDLE, -1);
        long assignmentId = intent.getLongExtra(AppConstants.KEY_ASSIGNMENT_BUNDLE, -1);
        if (stepId == -1 || assignmentId == -1) return;

        try {
            Response<Void> response = mApi.postViewed(new ViewAssignment(assignmentId, stepId)).execute();
            if (!response.isSuccess()) {
                throw new IOException("response is not success");
            }
        } catch (IOException e) {
            e.printStackTrace();

            //if we not push:
            mDb.addToQueueViewedState(new ViewAssignment(assignmentId, stepId));
        }

        //anyway check in db as viewed:
        mDb.markProgressAsPassed(assignmentId);
        mUnitProgressManager.checkUnitAsPassed(stepId);
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mBus.post(new UpdateStepEvent(stepId));
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }
}

