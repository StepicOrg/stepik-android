package org.stepik.android.data.features.repository

import io.reactivex.Single
import org.stepik.android.data.features.source.FeaturesRemoteDataSource
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.domain.feature.repository.FeaturesRepository
import javax.inject.Inject

class FeaturesRepositoryImpl @Inject constructor(
    private val featuresRemoteDataSource: FeaturesRemoteDataSource
) : FeaturesRepository {
    private var featuresCache: List<Feature>? = null
    private var inFlightRequest: Single<List<Feature>>? = null

    override fun getFeatures(forceUpdate: Boolean): Single<List<Feature>> {
        if (!forceUpdate) {
            getCachedFeatures()?.let { return Single.just(it) }
            getInFlightRequest()?.let { return it }
        }

        var request: Single<List<Feature>>? = null
        request = featuresRemoteDataSource
            .getFeatures()
            .doOnSuccess { setCachedFeaturesFromRequest(it, checkNotNull(request)) }
            .doFinally { clearInFlightRequest(checkNotNull(request)) }
            .cache()

        return getOrSetInFlightRequest(checkNotNull(request), forceUpdate)
    }

    override fun getCachedFeatures(): List<Feature>? =
        synchronized(this) {
            featuresCache
        }

    @Synchronized
    override fun replaceCachedFeatures(features: List<Feature>) {
        featuresCache = features
        inFlightRequest = null
    }

    @Synchronized
    private fun getInFlightRequest(): Single<List<Feature>>? =
        inFlightRequest

    @Synchronized
    private fun getOrSetInFlightRequest(request: Single<List<Feature>>, forceUpdate: Boolean): Single<List<Feature>> {
        if (!forceUpdate) {
            inFlightRequest?.let { return it }
        }

        inFlightRequest = request
        return request
    }

    @Synchronized
    private fun setCachedFeaturesFromRequest(features: List<Feature>, expected: Single<List<Feature>>) {
        if (inFlightRequest === expected) {
            featuresCache = features
        }
    }

    @Synchronized
    private fun clearInFlightRequest(expected: Single<List<Feature>>) {
        if (inFlightRequest === expected) {
            inFlightRequest = null
        }
    }
}
