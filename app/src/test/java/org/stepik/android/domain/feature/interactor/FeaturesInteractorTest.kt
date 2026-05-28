package org.stepik.android.domain.feature.interactor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.domain.feature.model.Parameters
import org.stepik.android.domain.feature.repository.FeaturesRepository

@RunWith(MockitoJUnitRunner::class)
class FeaturesInteractorTest {
    @Mock
    private lateinit var featuresRepository: FeaturesRepository

    @Test
    fun `isAuthMarketingAgreementEnabled returns true only for enabled feature`() {
        whenever(featuresRepository.getFeatures(false)) doReturn Single.just(
            listOf(createFeature(name = "AuthMarketingAgreement", isEnabled = true))
        )

        FeaturesInteractor(featuresRepository)
            .isAuthMarketingAgreementEnabled()
            .test()
            .assertComplete()
            .assertResult(true)
    }

    @Test
    fun `isAuthMarketingAgreementEnabled returns false for missing or incomplete feature data`() {
        whenever(featuresRepository.getFeatures(false)) doReturn Single.just(
            listOf(
                createFeature(name = "AuthMarketingAgreement", isEnabled = null),
                createFeature(id = 2, name = "Rubricator", cacheFile = "rubricator.json")
            )
        )

        FeaturesInteractor(featuresRepository)
            .isAuthMarketingAgreementEnabled()
            .test()
            .assertComplete()
            .assertResult(false)
    }

    @Test
    fun `isAuthMarketingAgreementEnabled returns false on repository failure`() {
        whenever(featuresRepository.getFeatures(false)) doReturn Single.error<List<Feature>>(IllegalStateException("error"))

        FeaturesInteractor(featuresRepository)
            .isAuthMarketingAgreementEnabled()
            .test()
            .assertComplete()
            .assertResult(false)
    }

    @Test
    fun `isAuthMarketingAgreementEnabledCached uses cached features and defaults to false`() {
        whenever(featuresRepository.getCachedFeatures()) doReturn null

        val interactor = FeaturesInteractor(featuresRepository)

        assertFalse(interactor.isAuthMarketingAgreementEnabledCached())

        whenever(featuresRepository.getCachedFeatures()) doReturn listOf(
            createFeature(name = "AuthMarketingAgreement", isEnabled = true)
        )

        assertTrue(interactor.isAuthMarketingAgreementEnabledCached())
    }

    @Test
    fun `fetchRubricatorUrl keeps returning empty string when feature is missing`() {
        whenever(featuresRepository.getFeatures(false)) doReturn Single.just(emptyList())

        FeaturesInteractor(featuresRepository)
            .fetchRubricatorUrl()
            .test()
            .assertComplete()
            .assertResult("")
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
}
