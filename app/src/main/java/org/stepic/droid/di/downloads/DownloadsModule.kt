package org.stepic.droid.di.downloads

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.core.downloads.DownloadsPosterImpl
import org.stepic.droid.core.downloads.contract.DownloadsListener
import org.stepic.droid.core.downloads.contract.DownloadsPoster
import java.util.concurrent.Executors

@Module
abstract class DownloadsModule {

    @Binds
    @DownloadsScope
    abstract fun bindsClient(clientImpl: ClientImpl<DownloadsListener>): Client<DownloadsListener>

    @Binds
    @DownloadsScope
    abstract fun bindContainer(listenerContainer: ListenerContainerImpl<DownloadsListener>): ListenerContainer<DownloadsListener>

    @Binds
    @DownloadsScope
    abstract fun bindsPoster(posterImpl: DownloadsPosterImpl): DownloadsPoster

    @Module
    companion object {

        @Provides
        @JvmStatic
        @DownloadsScope
        internal fun provideSingle(): SingleThreadExecutor =
                SingleThreadExecutor(Executors.newSingleThreadExecutor())
    }

}
