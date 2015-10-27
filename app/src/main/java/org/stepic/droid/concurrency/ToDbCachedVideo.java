package org.stepic.droid.concurrency;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.store.operations.DatabaseManager;

import javax.inject.Inject;

public class ToDbCachedVideo extends StepicTask<Void, Void, Void> {

    @Inject
    DatabaseManager databaseManager;

    private CachedVideo cachedVideo;

    public ToDbCachedVideo(CachedVideo cachedVideo) {
        super(MainApplication.getAppContext());
        MainApplication.component().inject(this);
        this.cachedVideo = cachedVideo;

    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        databaseManager.addVideo(cachedVideo);
        return null;
    }
}
