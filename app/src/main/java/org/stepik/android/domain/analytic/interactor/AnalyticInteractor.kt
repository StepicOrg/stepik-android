package org.stepik.android.domain.analytic.interactor

import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
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
        private const val PARAM_QUERY = "query"
    }

    fun logEvent(eventName: String, bundle: Bundle): Completable =
        Single
            .fromCallable {
                val properties = JsonObject()
                bundle.keySet()?.forEach {
                    properties.add(it, gson.toJsonTree(bundle[it]))
                }
                setLanguageProperty(properties)
                return@fromCallable properties
            }
            .flatMapCompletable {
                val analyticEvent =
                    AnalyticLocalEvent(
                        name = eventName,
                        eventData = it,
                        eventTimestamp = DateTimeHelper.nowUtc()
                    )
                analyticRepository.logEvent(analyticEvent)
            }

    fun flushEvents(): Completable =
        analyticRepository.flushEvents()

    /**
     * If we have a query object, we obtain the language value from it. If it is null, we don't add anything (language == null
     * means that we are searching for courses in ALL languages.
     * If there is no query object we obtain the default filter language.
     */
    private fun setLanguageProperty(properties: JsonObject) {
        val queryObject = properties
            .getAsJsonObject(PARAM_DATA)
            ?.getAsJsonObject(PARAM_DATA)
            ?.getAsJsonObject(PARAM_QUERY)

        val language = if (queryObject == null) {
            sharedPreferencesHelper.languageForFeatured
        } else {
            queryObject.get(PARAM_LANGUAGE)?.asString
        }

        if (language != null) {
            (properties[PARAM_DATA] as? JsonObject)?.add(PARAM_LANGUAGE, JsonPrimitive(language))
        }
    }
}