package org.stepik.android.view.injection.course

import dagger.Module
import dagger.Provides
import org.stepik.android.data.course.repository.CoursePurchaseDataRepositoryImpl
import org.stepik.android.domain.course.repository.CoursePurchaseDataRepository

@Module
abstract class CoursePurchaseDataModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseScope
        fun provideCoursePurchaseDataRepository(): CoursePurchaseDataRepository =
            CoursePurchaseDataRepositoryImpl()
    }
}