package org.stepic.droid.services;

import android.app.IntentService;
import android.content.Intent;

import org.stepic.droid.base.MainApplication;

@Deprecated
public class LoadService extends IntentService {





    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p/>
     * name Used to name the worker thread, important only for debugging.
     */
    public LoadService() {
        super("Loading_video_service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainApplication.component().inject(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
