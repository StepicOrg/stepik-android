package org.stepic.droid.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.WorkerThread;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.internetstate.contract.InternetEnabledPoster;
import org.stepik.android.domain.progress.interactor.LocalProgressInteractor;
import org.stepik.android.model.Step;
import org.stepic.droid.model.ViewedNotification;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.StepHelper;
import org.stepic.droid.web.Api;
import org.stepik.android.model.ViewAssignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class InternetConnectionEnabledReceiver extends BroadcastReceiver {


    @Inject
    Api api;

    @Inject
    DatabaseFacade databaseFacade;

    @Inject
    Analytic analytic;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    @Inject
    LocalProgressInteractor localProgressInteractor;

    @Inject
    MainHandler mainHandler;

    @Inject
    InternetEnabledPoster internetEnabledPoster;

    private AtomicBoolean inWork = new AtomicBoolean(false);

    private static Boolean isLastStateWasOffline = null;


    public InternetConnectionEnabledReceiver() {
        App.Companion.component().inject(this);
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        if (!isDeviceChangeStateToOnline(App.Companion.getAppContext()) || inWork.get()) return;
        inWork.set(true);
        mainHandler.post(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                internetEnabledPoster.internetEnabled();
                return Unit.INSTANCE;
            }
        });


        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                processViewAssignments();
                processViewedNotifications();
                inWork.set(false);
            }
        });
    }

    @WorkerThread
    private void processViewedNotifications() {
        List<ViewedNotification> viewedNotifications = databaseFacade.getViewedNotificationsQueue();
        for (ViewedNotification viewedNotification : viewedNotifications) {
            try {
                retrofit2.Response<Void> response
                        = api.setReadStatusForNotification(viewedNotification.getNotificationId(), true).execute();

                if (response.isSuccessful()) {
                    databaseFacade.removeViewedNotification(viewedNotification);
                }
            } catch (IOException e) {
                //no internet, just ignore and send next time
            }
        }
    }

    @WorkerThread
    private void processViewAssignments() {
        List<ViewAssignment> list = databaseFacade.getAllInQueue();
        List<Step> updatedSteps = new ArrayList<>();
        for (ViewAssignment item : list) {
            try {
                retrofit2.Response<Void> response = api.postViewed(item).execute();
                if (response.isSuccessful()) {
                    databaseFacade.removeFromQueue(item);
                    Step step = databaseFacade.getStepById(item.getStep());
                    if (step != null) {
                        final long stepId = step.getId();
                        if (StepHelper.isViewedStatePost(step)) {
                            if (item.getAssignment() != null) {
                                databaseFacade.markProgressAsPassed(item.getAssignment());
                            } else {
                                if (step.getProgress() != null) {
                                    databaseFacade.markProgressAsPassedIfInDb(step.getProgress());
                                }
                            }
                        }
                        // Get a handler that can be used to post to the main thread

                    }
                    updatedSteps.add(step);
                }
            } catch (IOException e) {
                //no internet, just ignore and send next time
            }
        }

        try {
            localProgressInteractor
                    .updateStepsProgress(updatedSteps)
                    .blockingAwait();
        } catch (Exception e) {
            //no internet, just ignore and send next time
        }
    }

    private boolean isDeviceChangeStateToOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in air plan mode it will be null
        boolean isOnlineNow = netInfo != null && netInfo.isConnected();
        if (isOnlineNow && (isLastStateWasOffline == null || isLastStateWasOffline)) {
            isLastStateWasOffline = false;
            return true;
        } else {
            isLastStateWasOffline = true;
            return false;
        }
    }

}
