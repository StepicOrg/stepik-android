package org.stepic.droid.di.downloads

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.downloads.DownloadsPosterImpl
import org.stepic.droid.core.downloads.contract.DownloadsListener
import org.stepic.droid.core.downloads.contract.DownloadsPoster

@Module
interface DownloadsModule {

    @Binds
    @DownloadsScope
    fun bindsClient(clientImpl: ClientImpl<DownloadsListener>): Client<DownloadsListener>

    @Binds
    @DownloadsScope
    fun bindContainer(listenerContainer: ListenerContainerImpl<DownloadsListener>): ListenerContainer<DownloadsListener>

    @Binds
    @DownloadsScope
    fun bindsPoster(posterImpl: DownloadsPosterImpl): DownloadsPoster
}
