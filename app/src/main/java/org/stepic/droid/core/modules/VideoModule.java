package org.stepic.droid.core.modules;

import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.VideoWithTimestampPresenter;
import org.stepic.droid.storage.operations.DatabaseFacade;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class VideoModule {

    @Provides
    @PerFragment
    public VideoWithTimestampPresenter provideVideoTimestampPresenter(DatabaseFacade databaseFacade,
                                                                      MainHandler mainHandler,
                                                                      ThreadPoolExecutor threadPoolExecutor) {
        return new VideoWithTimestampPresenter(
                databaseFacade,
                mainHandler,
                threadPoolExecutor);
    }

}
