package org.stepic.droid.util.resolvers

import android.net.Uri
import org.stepic.droid.util.appendQueryParameters
import javax.inject.Inject

class UriResolver
@Inject
constructor() {
    fun resolveUriThroughUrl(url: String, queryParamMap: Map<String, List<String>>?): Uri {
        val uriBuilder = Uri.parse(url)
            .buildUpon()
            .appendQueryParameter("from_mobile_app", "true")

        if (queryParamMap != null) {
            uriBuilder.appendQueryParameters(queryParamMap)
        }
        return uriBuilder.build()
    }
}