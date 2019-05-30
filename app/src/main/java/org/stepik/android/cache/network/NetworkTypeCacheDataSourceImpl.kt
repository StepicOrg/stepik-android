package org.stepik.android.cache.network

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.preferences.UserPreferences
import org.stepik.android.cache.network.tracker.NetworkStateTracker
import org.stepik.android.data.network.source.NetworkTypeCacheDataSource
import java.util.EnumSet
import javax.inject.Inject

class NetworkTypeCacheDataSourceImpl
@Inject
constructor(
    private val userPreferences: UserPreferences,
    private val networkStateTracker: NetworkStateTracker
) : NetworkTypeCacheDataSource {
    override fun getAvailableNetworkTypesStream(): Observable<EnumSet<DownloadConfiguration.NetworkType>> =
        Observable.create { emitter ->
            val listener = emitter::onNext
            networkStateTracker.addListener(listener)

            emitter.setCancellable { networkStateTracker.removeListener(listener) }
        }

    override fun getAvailableNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>> =
        Single.fromCallable(networkStateTracker::getNetworkState)

    override fun getAllowedNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>> =
        Single.fromCallable {
            if (userPreferences.isNetworkMobileAllowed) {
                EnumSet.of(DownloadConfiguration.NetworkType.MOBILE, DownloadConfiguration.NetworkType.WIFI)
            } else {
                EnumSet.of(DownloadConfiguration.NetworkType.WIFI)
            }
        }
}