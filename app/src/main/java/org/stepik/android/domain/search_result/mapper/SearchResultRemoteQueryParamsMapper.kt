package org.stepik.android.domain.search_result.mapper

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.stepic.droid.configuration.RemoteConfig
import javax.inject.Inject

class SearchResultRemoteQueryParamsMapper
@Inject
constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val gson: Gson
) {
    fun buildRemoteQueryParams(): Map<String, Any> {
        val queryParamsString = firebaseRemoteConfig.getString(RemoteConfig.SEARCH_QUERY_PARAMS_ANDROID)
        return gson.fromJson(queryParamsString, TypeToken.getParameterized(Map::class.java, String::class.java, Any::class.java).type)
    }
}