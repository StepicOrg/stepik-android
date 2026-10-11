package org.stepik.android.view.injection.debug

import android.content.Context
import dagger.Module
import dagger.Provides
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.endpoint.BackendFeaturesDebugEndpointProvider
import org.stepik.android.domain.debug.endpoint.BackendFeaturesDebugEndpointProviderImpl
import org.stepik.android.domain.debug.storage.BackendFeaturesDebugStorage
import org.stepik.android.domain.debug.storage.BackendFeaturesDebugStorageImpl

@Module
object BackendFeaturesPresentationModule {
    @Provides
    internal fun provideBackendFeaturesDebugStorage(context: Context): BackendFeaturesDebugStorage =
        BackendFeaturesDebugStorageImpl(context)

    @Provides
    internal fun provideBackendFeaturesDebugEndpointProvider(
        sharedPreferenceHelper: SharedPreferenceHelper
    ): BackendFeaturesDebugEndpointProvider =
        BackendFeaturesDebugEndpointProviderImpl(sharedPreferenceHelper)
}
