package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.StepQualityView
import org.stepic.droid.di.step.StepScope
import org.stepic.droid.model.Step
import org.stepic.droid.model.Video
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.storage.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@StepScope
class StepQualityPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val databaseFacade: DatabaseFacade,
        private val userPreferences: UserPreferences,
        private val analytic: Analytic) : PresenterBase<StepQualityView>() {

    fun determineQuality(stepVideo: Video?) {
        if (stepVideo == null) {
            analytic.reportEvent(Analytic.Video.QUALITY_NOT_DETERMINATED)
        } else {
            threadPoolExecutor.execute {
                val video = databaseFacade.getCachedVideoById(stepVideo.id)
                val quality: String
                if (video == null) {
                    val resultQuality: String = try {
                        val weWant = Integer.parseInt(userPreferences.qualityVideo)
                        val urls = stepVideo.urls //TODO: URLS can be empty here: Try to research it
                        var bestDelta = Integer.MAX_VALUE
                        var bestIndex = 0
                        for (i in urls.indices) {
                            val current = Integer.parseInt(urls[i].quality)
                            val delta = Math.abs(current - weWant)
                            if (delta < bestDelta) {
                                bestDelta = delta
                                bestIndex = i
                            }

                        }
                        urls[bestIndex].quality
                    } catch (e: Exception) {
                        userPreferences.qualityVideo
                    }

                    quality = resultQuality
                } else {
                    quality = video.quality
                }
                if (quality.isNullOrBlank()) {
                    analytic.reportEvent(Analytic.Video.QUALITY_NOT_DETERMINATED)
                } else {
                    val qualityForView = quality + "p"
                    mainHandler.post {
                        view?.showQuality(qualityForView)
                    }
                }
            }
        }

    }

    fun determineQuality(step: Step?) {
        step?.block?.let { block ->
            determineQuality(block.video)
        }
    }
}
