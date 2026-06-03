package org.stepik.android.domain.feature.interactor

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import javax.inject.Inject

@AppSingleton
class FeaturesPreloader @Inject constructor(
    private val featuresInteractor: FeaturesInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler
) {
    fun preload() {
        featuresInteractor
            .preloadFeatures()
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = {},
                onError = {}
            )
    }
}
