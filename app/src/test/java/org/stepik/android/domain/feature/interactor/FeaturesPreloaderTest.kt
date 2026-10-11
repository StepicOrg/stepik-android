package org.stepik.android.domain.feature.interactor

import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.domain.feature.repository.FeaturesRepository

class FeaturesPreloaderTest {
    @Test
    fun `preload subscribes on background scheduler`() {
        var subscribed = false
        val scheduler = TestScheduler()
        val repository = object : FeaturesRepository {
            override fun getFeatures(forceUpdate: Boolean): Single<List<Feature>> =
                Single.fromCallable {
                    subscribed = true
                    emptyList()
                }

            override fun getCachedFeatures(): List<Feature>? =
                null

            override fun replaceCachedFeatures(features: List<Feature>) {}
        }
        val preloader = FeaturesPreloader(FeaturesInteractor(repository), scheduler)

        preloader.preload()

        assertFalse(subscribed)

        scheduler.triggerActions()

        assertTrue(subscribed)
    }

    @Test
    fun `preload swallows feature loading errors`() {
        val scheduler = TestScheduler()
        val repository = object : FeaturesRepository {
            override fun getFeatures(forceUpdate: Boolean): Single<List<Feature>> =
                Single.error(IllegalStateException("boom"))

            override fun getCachedFeatures(): List<Feature>? =
                null

            override fun replaceCachedFeatures(features: List<Feature>) {}
        }
        val preloader = FeaturesPreloader(FeaturesInteractor(repository), scheduler)

        preloader.preload()

        scheduler.triggerActions()
    }
}
