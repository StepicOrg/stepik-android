package org.stepik.android.view.injection.analytic

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.storage.StorageComponent
import org.stepik.android.view.analytic.AnalyticContentProvider

@Component(
    dependencies = [
        StorageComponent::class
    ],
    modules = [
        AnalyticModule::class
    ]
)
@AppSingleton
interface AnalyticComponent {
    @Component.Builder
    interface Builder {
        fun build(): AnalyticComponent

        fun setStorageComponent(storageComponent: StorageComponent): Builder

        @BindsInstance
        fun context(context: Context): Builder
    }

    fun inject(analyticContentProvider: AnalyticContentProvider)
}