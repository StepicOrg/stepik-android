package org.stepik.android.view.injection.analytic

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.ConfigModule
import org.stepic.droid.di.NotificationsBadgesModule
import org.stepic.droid.di.storage.StorageComponent
import org.stepic.droid.persistence.di.PersistenceModule
import org.stepik.android.view.analytic.AnalyticContentProvider
import org.stepik.android.view.injection.course.CourseRoutingModule
import org.stepik.android.view.injection.network.NetworkModule

@Component(
    dependencies = [
        StorageComponent::class
    ],
    modules = [
        CourseRoutingModule::class,
        PersistenceModule::class,
        NotificationsBadgesModule::class,
        ConfigModule::class,
        NetworkModule::class,
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