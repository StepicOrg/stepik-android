package org.stepik.android.domain.network.repository

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.persistence.model.DownloadConfiguration
import java.util.EnumSet

interface NetworkTypeRepository {
    /**
     * Returns available network types at current moment
     */
    fun getAvailableNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>>

    /**
     * Returns stream of network types changes
     */
    fun getAvailableNetworkTypesStream(): Observable<EnumSet<DownloadConfiguration.NetworkType>>

    /**
     * Returns network types allowed to download over by user
     */
    fun getAllowedNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>>
}