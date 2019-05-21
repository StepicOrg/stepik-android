package org.stepic.droid.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;

import org.stepic.droid.base.App;
import org.stepic.droid.concurrency.MainHandler;
import org.stepik.android.domain.progress.interactor.LocalProgressInteractor;
import org.stepik.android.model.Step;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.resolvers.StepHelper;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.web.Api;
import org.stepik.android.model.ViewAssignment;

import java.io.IOException;

import javax.inject.Inject;

import kotlin.collections.CollectionsKt;
import retrofit2.Response;

public class ViewPusher extends IntentService {

    @Inject
    DownloadManager systemDownloadManager;
    @Inject
    UserPreferences userPrefs;
    @Inject
    VideoResolver resolver;
    @Inject
    Api api;
    @Inject
    DatabaseFacade database;

    @Inject
    LocalProgressInteractor localProgressInteractor;

    @Inject
    MainHandler mainHandler;


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
        App.Companion.component().inject(this);
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
            if (!response.isSuccessful()) {
                throw new IOException("response is not success");
            }
        } catch (IOException e) {
            //if we not push:
            database.addToQueueViewedState(new ViewAssignment(assignmentId, stepId));
        }

        Step step = database.getStepById(stepId);
        if (step != null) {
            try {
                localProgressInteractor.updateStepsProgress(CollectionsKt.listOf(step)).blockingAwait();
            } catch (Exception e) {
                // no op
            }
        }

        //check in db as passed if it can be passed by view
        if (StepHelper.isViewedStatePost(step)) {
            if (assignmentId != null) {
                database.markProgressAsPassed(assignmentId);
            }
        }
        // Get a handler that can be used to post to the main thread
    }
}

