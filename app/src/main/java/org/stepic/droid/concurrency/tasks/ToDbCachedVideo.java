package org.stepic.droid.concurrency.tasks;

import android.content.Context;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.store.operations.DatabaseFacade;

import javax.inject.Inject;

public class ToDbCachedVideo extends StepicTask<Void, Void, Void> {

    @Inject
    DatabaseFacade mDatabaseFacade;

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
        mDatabaseFacade.addVideo(cachedVideo);
        return null;
    }
}
