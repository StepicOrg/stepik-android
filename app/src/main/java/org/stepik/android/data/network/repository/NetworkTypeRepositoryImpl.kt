package org.stepik.android.data.network.repository

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepik.android.data.network.source.NetworkTypeCacheDataSource
import org.stepik.android.domain.network.repository.NetworkTypeRepository
import java.util.EnumSet
import javax.inject.Inject

class NetworkTypeRepositoryImpl
@Inject
constructor(
    private val networkTypeCacheDataSource: NetworkTypeCacheDataSource
) : NetworkTypeRepository {

    override fun getAvailableNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>> =
        networkTypeCacheDataSource.getAvailableNetworkTypes()

    override fun getAvailableNetworkTypesStream(): Observable<EnumSet<DownloadConfiguration.NetworkType>> =
        networkTypeCacheDataSource.getAvailableNetworkTypesStream()

    override fun getAllowedNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>> =
        networkTypeCacheDataSource.getAllowedNetworkTypes()
}