package org.stepic.droid.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import com.squareup.otto.Bus;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.ViewAssignment;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class InternetConnectionEnabledReceiver extends BroadcastReceiver {


    @Inject
    IApi mApi;
    @Inject
    DatabaseManager databaseManager;
    @Inject
    IStoreStateManager mStoreStateManager;

    @Inject
    Bus bus;

    private volatile boolean inWork;


    public InternetConnectionEnabledReceiver() {
        MainApplication.component().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isOnline(MainApplication.getAppContext()) || inWork) return;
        inWork = true;

        Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                bus.post(new InternetIsEnabledEvent());
            }
        };
        mainHandler.post(myRunnable);

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<ViewAssignment> list = databaseManager.getAllInQueue();
                for (ViewAssignment item : list) {
                    try {
                        retrofit.Response<Void> response = mApi.postViewed(item).execute();
                        if (response.isSuccess()) {
                            databaseManager.removeFromQueue(item);
                        }
                    } catch (IOException e) {
                        YandexMetrica.reportError("Push state exception", e);
                        e.printStackTrace();
                    }
                }
                inWork = false;

                return null;
            }
        };
        task.execute();

    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in air plan mode it will be null
        return (netInfo != null && netInfo.isConnected());

    }

}
