package org.stepic.droid.di.downloads

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.core.downloads.contract.DownloadsListener
import java.util.concurrent.Executors

@Module
abstract class DownloadsModule {

    @Binds
    @DownloadsScope
    abstract fun bindsClient(clientImpl: ClientImpl<DownloadsListener>): Client<DownloadsListener>

    @Binds
    @DownloadsScope
    abstract fun bindContainer(listenerContainer: ListenerContainerImpl<DownloadsListener>): ListenerContainer<DownloadsListener>

    @Module
    companion object {

        @Provides
        @JvmStatic
        @DownloadsScope
        internal fun provideSingle(): SingleThreadExecutor =
                SingleThreadExecutor(Executors.newSingleThreadExecutor())
    }

}
