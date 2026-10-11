package org.stepik.android.data.features.repository

import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.stepik.android.data.features.source.FeaturesRemoteDataSource
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.domain.feature.model.Parameters

class FeaturesRepositoryImplTest {
    @Test
    fun `getFeatures caches successful result and force update refreshes it`() {
        val firstFeatures = listOf(createFeature(name = "AuthMarketingAgreement", isEnabled = true))
        val refreshedFeatures = listOf(createFeature(name = "Rubricator", cacheFile = "refreshed.json"))
        val remoteDataSource = FakeFeaturesRemoteDataSource(
            Single.just(firstFeatures),
            Single.just(refreshedFeatures)
        )
        val repository = FeaturesRepositoryImpl(remoteDataSource)

        repository.getFeatures().test()
            .assertComplete()
            .assertResult(firstFeatures)

        repository.getFeatures().test()
            .assertComplete()
            .assertResult(firstFeatures)

        repository.getFeatures(forceUpdate = true).test()
            .assertComplete()
            .assertResult(refreshedFeatures)

        assertEquals(2, remoteDataSource.requestsCount)
        assertEquals(refreshedFeatures, repository.getCachedFeatures())
        assertNull(repository.readInFlightRequestForTest())
    }

    @Test
    fun `getFeatures shares in flight request between subscribers and clears it after success`() {
        val features = listOf(createFeature(name = "AuthMarketingAgreement", isEnabled = true))
        val subject = SingleSubject.create<List<Feature>>()
        val remoteDataSource = FakeFeaturesRemoteDataSource(subject)
        val repository = FeaturesRepositoryImpl(remoteDataSource)

        val firstObserver = repository.getFeatures().test()
        val secondObserver = repository.getFeatures().test()

        assertEquals(1, remoteDataSource.requestsCount)
        subject.onSuccess(features)

        firstObserver.assertComplete().assertResult(features)
        secondObserver.assertComplete().assertResult(features)

        assertEquals(features, repository.getCachedFeatures())
        assertNull(repository.readInFlightRequestForTest())
    }

    @Test
    fun `getFeatures clears failed in flight request and does not cache error`() {
        val error = RuntimeException("boom")
        val features = listOf(createFeature(name = "Rubricator", cacheFile = "rubricator.json"))
        val remoteDataSource = FakeFeaturesRemoteDataSource(
            Single.error<List<Feature>>(error),
            Single.just(features)
        )
        val repository = FeaturesRepositoryImpl(remoteDataSource)

        repository.getFeatures().test()
            .assertError(error)

        assertNull(repository.getCachedFeatures())
        assertNull(repository.readInFlightRequestForTest())

        repository.getFeatures().test()
            .assertComplete()
            .assertResult(features)

        assertEquals(2, remoteDataSource.requestsCount)
        assertEquals(features, repository.getCachedFeatures())
    }

    @Test
    fun `replaceCachedFeatures updates cache and ignores stale in flight response`() {
        val remoteFeatures = listOf(createFeature(name = "AuthMarketingAgreement", isEnabled = false))
        val replacementFeatures = listOf(createFeature(name = "AuthMarketingAgreement", isEnabled = true))
        val subject = SingleSubject.create<List<Feature>>()
        val remoteDataSource = FakeFeaturesRemoteDataSource(subject)
        val repository = FeaturesRepositoryImpl(remoteDataSource)

        val observer = repository.getFeatures().test()

        repository.replaceCachedFeatures(replacementFeatures)

        assertEquals(replacementFeatures, repository.getCachedFeatures())
        assertNull(repository.readInFlightRequestForTest())

        subject.onSuccess(remoteFeatures)

        observer.assertComplete().assertResult(remoteFeatures)
        assertEquals(replacementFeatures, repository.getCachedFeatures())
    }

    private fun createFeature(
        id: Long = 1,
        name: String,
        cacheFile: String? = null,
        isEnabled: Boolean? = null
    ): Feature =
        Feature(
            id = id,
            name = name,
            parameters = Parameters(
                cacheFile = cacheFile,
                isEnabled = isEnabled
            )
        )

    private fun FeaturesRepositoryImpl.readInFlightRequestForTest(): Single<List<Feature>>? =
        FeaturesRepositoryImpl::class.java
            .getDeclaredField("inFlightRequest")
            .apply { isAccessible = true }
            .get(this) as? Single<List<Feature>>

    private class FakeFeaturesRemoteDataSource(
        vararg responses: Single<List<Feature>>
    ) : FeaturesRemoteDataSource {
        private val queuedResponses = ArrayDeque(responses.toList())

        var requestsCount: Int = 0
            private set

        override fun getFeatures(): Single<List<Feature>> {
            requestsCount++
            return queuedResponses.removeFirstOrNull()
                ?: error("No queued feature response for request #$requestsCount")
        }
    }
}
