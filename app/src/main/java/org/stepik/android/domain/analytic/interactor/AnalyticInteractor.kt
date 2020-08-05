package org.stepik.android.domain.analytic.interactor

import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent
import org.stepik.android.domain.analytic.repository.AnalyticRepository
import javax.inject.Inject

class AnalyticInteractor
@Inject
constructor(
    private val sharedPreferencesHelper: SharedPreferenceHelper,
    private val analyticRepository: AnalyticRepository,
    private val gson: Gson
) {
    companion object {
        private const val PARAM_DATA = "data"
        private const val PARAM_LANGUAGE = "language"
    }

    private val jsonParser = JsonParser()

    fun logEvent(eventName: String, bundle: Bundle): Completable =
        Single.fromCallable {
            val properties: HashMap<String, JsonElement> = HashMap()
            bundle.keySet()?.forEach {
                properties[it] = jsonParser.parse(java.lang.String.valueOf(bundle[it]))
            }
            (properties[PARAM_DATA] as JsonObject).add(PARAM_LANGUAGE, jsonParser.parse(sharedPreferencesHelper.languageForFeatured))
            return@fromCallable properties
        }.flatMapCompletable {
            val analyticEvent =
                AnalyticLocalEvent(
                    name = eventName,
                    eventData = gson.toJsonTree(it),
                    eventTimestamp = DateTimeHelper.nowUtc()
                )
            analyticRepository.logEvent(analyticEvent)
        }

    fun flushEvents(): Completable =
        analyticRepository.flushEvents()
}