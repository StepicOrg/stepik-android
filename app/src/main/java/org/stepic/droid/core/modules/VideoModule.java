package org.stepic.droid.core.modules;

import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.VideoWIthTimestampPresenter;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class VideoModule {

    @Provides
    @PerFragment
    public VideoWIthTimestampPresenter provideVideoTimestampPresenter(DatabaseFacade databaseFacade,
                                                                      IMainHandler mainHandler,
                                                                      ThreadPoolExecutor threadPoolExecutor) {
        return new VideoWIthTimestampPresenter(
                databaseFacade,
                mainHandler,
                threadPoolExecutor);
    }

}
