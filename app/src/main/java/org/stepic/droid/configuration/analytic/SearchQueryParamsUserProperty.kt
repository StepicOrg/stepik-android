package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class SearchQueryParamsUserProperty(searchQueryParams: String) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.SEARCH_QUERY_PARAMS_ANDROID

    override val value: String =
        searchQueryParams
}