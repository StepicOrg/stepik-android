package org.stepik.android.domain.debug.interactor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.stepik.android.domain.debug.endpoint.BackendFeaturesDebugEndpointProvider
import org.stepik.android.domain.debug.model.AuthMarketingAgreementDebugOverride
import org.stepik.android.domain.debug.model.EndpointConfig
import org.stepik.android.domain.debug.storage.BackendFeaturesDebugStorage
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.domain.feature.model.Parameters
import org.stepik.android.domain.feature.repository.FeaturesRepository

class BackendFeaturesDebugInteractorTest {
    private lateinit var featuresRepository: FakeFeaturesRepository
    private lateinit var endpointProvider: BackendFeaturesDebugEndpointProvider
    private lateinit var backendFeaturesDebugStorage: BackendFeaturesDebugStorage
    private lateinit var interactor: BackendFeaturesDebugInteractor

    @Before
    fun setUp() {
        featuresRepository = FakeFeaturesRepository(
            listOf(
                createFeature(name = "AuthMarketingAgreement", isEnabled = false),
                createFeature(id = 2, name = "Rubricator", cacheFile = "server.json")
            )
        )
        endpointProvider = FakeEndpointProvider(EndpointConfig.PRODUCTION)
        backendFeaturesDebugStorage = mock {
            on { getAuthMarketingAgreementOverride(EndpointConfig.PRODUCTION) } doReturn AuthMarketingAgreementDebugOverride.SERVER
            on { getRubricatorOverrideCacheFile(EndpointConfig.PRODUCTION) } doReturn null
        }
        interactor = BackendFeaturesDebugInteractor(
            featuresRepository,
            endpointProvider,
            backendFeaturesDebugStorage
        )
    }

    @Test
    fun `forced auth marketing enabled updates active feature cache`() {
        whenever(backendFeaturesDebugStorage.getAuthMarketingAgreementOverride(EndpointConfig.PRODUCTION)) doReturn
            AuthMarketingAgreementDebugOverride.FORCE_ENABLED

        val data = interactor
            .getDebugData(forceUpdate = true)
            .blockingGet()

        assertEquals(false, data.serverAuthMarketingAgreement)
        assertTrue(data.activeAuthMarketingAgreement)
        assertEquals(true, featuresRepository.cachedAuthMarketingAgreement())
    }

    @Test
    fun `setting auth marketing override persists endpoint scoped value and applies to cache`() {
        whenever(backendFeaturesDebugStorage.getAuthMarketingAgreementOverride(EndpointConfig.PRODUCTION)) doReturn
            AuthMarketingAgreementDebugOverride.FORCE_DISABLED

        val data = interactor.setAuthMarketingAgreementOverride(
            AuthMarketingAgreementDebugOverride.FORCE_DISABLED,
            featuresRepository.remoteFeatures
        )

        verify(backendFeaturesDebugStorage).setAuthMarketingAgreementOverride(
            EndpointConfig.PRODUCTION,
            AuthMarketingAgreementDebugOverride.FORCE_DISABLED
        )
        assertFalse(data.activeAuthMarketingAgreement)
        assertEquals(false, featuresRepository.cachedAuthMarketingAgreement())
    }

    @Test
    fun `rubricator override updates active cache file without changing server value`() {
        whenever(backendFeaturesDebugStorage.getRubricatorOverrideCacheFile(EndpointConfig.PRODUCTION)) doReturn
            "debug-rubricator.json"

        val data = interactor
            .getDebugData(forceUpdate = true)
            .blockingGet()

        assertEquals("server.json", data.serverRubricatorCacheFile)
        assertEquals("debug-rubricator.json", data.activeRubricatorCacheFile)
        assertEquals("debug-rubricator.json", featuresRepository.cachedRubricatorCacheFile())
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

    private class FakeFeaturesRepository(
        val remoteFeatures: List<Feature>
    ) : FeaturesRepository {
        private var cachedFeatures: List<Feature>? = null

        override fun getFeatures(forceUpdate: Boolean): Single<List<Feature>> =
            Single.just(remoteFeatures)

        override fun getCachedFeatures(): List<Feature>? =
            cachedFeatures

        override fun replaceCachedFeatures(features: List<Feature>) {
            cachedFeatures = features
        }

        fun cachedAuthMarketingAgreement(): Boolean? =
            cachedFeatures
                ?.firstOrNull { it.name == "AuthMarketingAgreement" }
                ?.parameters
                ?.isEnabled

        fun cachedRubricatorCacheFile(): String? =
            cachedFeatures
                ?.firstOrNull { it.name == "Rubricator" }
                ?.parameters
                ?.cacheFile
    }

    private class FakeEndpointProvider(
        private val endpointConfig: EndpointConfig
    ) : BackendFeaturesDebugEndpointProvider {
        override fun getEndpointConfig(): EndpointConfig =
            endpointConfig
    }
}
