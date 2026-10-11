package org.stepik.android.domain.debug.endpoint

import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.EndpointConfig
import javax.inject.Inject

interface BackendFeaturesDebugEndpointProvider {
    fun getEndpointConfig(): EndpointConfig
}

class BackendFeaturesDebugEndpointProviderImpl
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : BackendFeaturesDebugEndpointProvider {
    override fun getEndpointConfig(): EndpointConfig =
        EndpointConfig.values()[sharedPreferenceHelper.endpointConfig]
}
