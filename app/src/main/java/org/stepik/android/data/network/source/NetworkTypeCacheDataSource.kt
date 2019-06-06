package org.stepik.android.data.network.source

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.persistence.model.DownloadConfiguration
import java.util.EnumSet

interface NetworkTypeCacheDataSource {
    fun getAvailableNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>>

    fun getAvailableNetworkTypesStream(): Observable<EnumSet<DownloadConfiguration.NetworkType>>

    fun getAllowedNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>>
}