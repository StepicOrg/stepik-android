package org.stepic.droid.concurrency;

import android.content.Context;
import android.util.Log;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.store.operations.DatabaseManager;

import javax.inject.Inject;

public class ToDbCachedVideo extends StepicTask<Void, Void, Void> {

    @Inject
    DatabaseManager databaseManager;

    private CachedVideo cachedVideo;

    public ToDbCachedVideo(CachedVideo cachedVideo) {
        this(MainApplication.getAppContext(), cachedVideo);
    }

    public ToDbCachedVideo(Context context, CachedVideo cachedVideo) {
        super(context);
        MainApplication.component(context).inject(this);
        this.cachedVideo = cachedVideo;
    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        Log.i("downloading", "start task for video id " + cachedVideo.getVideoId());
        databaseManager.addVideo(cachedVideo);
        return null;
    }
}
