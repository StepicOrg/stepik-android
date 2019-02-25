package org.stepik.android.domain.network.repository

import io.reactivex.Single
import org.stepic.droid.persistence.model.DownloadConfiguration
import java.util.EnumSet

interface NetworkTypeRepository {
    fun getAvailableNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>>

    fun getAllowedNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>>
}