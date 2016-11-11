package org.stepic.droid.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.LocalProgressManager;
import org.stepic.droid.events.steps.UpdateStepEvent;
import org.stepic.droid.model.Step;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.util.resolvers.StepHelper;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.ViewAssignment;

import java.io.IOException;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import retrofit.Response;

public class ViewPusher extends IntentService {

    @Inject
    DownloadManager systemDownloadManager;
    @Inject
    UserPreferences userPrefs;
    @Inject
    Bus bus;
    @Inject
    VideoResolver resolver;
    @Inject
    IApi api;
    @Inject
    DatabaseFacade database;
    @Inject
    IStoreStateManager storeStateManager;

    @Inject
    LocalProgressManager unitProgressManager;

    @Inject
    IMainHandler mainHandler;


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
        Long assignmentId = intent.getLongExtra(AppConstants.KEY_ASSIGNMENT_BUNDLE, -1);
        if (stepId == -1) return;

        if (assignmentId < 0) {
            assignmentId = null;
        }

        try {
            Response<Void> response = api.postViewed(new ViewAssignment(assignmentId, stepId)).execute();
            if (!response.isSuccess()) {
                throw new IOException("response is not success");
            }
        } catch (IOException e) {
            //if we not push:
            database.addToQueueViewedState(new ViewAssignment(assignmentId, stepId));
        }

        Step step = database.getStepById(stepId);

        //check in db as passed if it can be passed by view
        if (StepHelper.isViewedStatePost(step)) {
            if (assignmentId != null) {
                database.markProgressAsPassed(assignmentId);
            } else {
                if (step != null && step.getProgressId() != null) {
                    database.markProgressAsPassedIfInDb(step.getProgressId());
                }
            }
            unitProgressManager.checkUnitAsPassed(stepId);
        }
        // Get a handler that can be used to post to the main thread

        mainHandler.post(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                bus.post(new UpdateStepEvent(stepId, false));
                return Unit.INSTANCE;
            }
        });
    }
}

