package org.stepik.android.view.injection.course

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.data.course.repository.CourseRepositoryImpl
import org.stepik.android.data.course.repository.EnrollmentRepositoryImpl
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.data.course.source.EnrollmentRemoteDataSource
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.presentation.base.injection.DaggerViewModelFactory
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.remote.course.source.CourseRemoteDataSourceImpl
import org.stepik.android.remote.course.source.EnrollmentRemoteDataSourceImpl

@Module
abstract class CourseModule {
    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(CoursePresenter::class)
    internal abstract fun bindCoursePresenter(coursePresenter: CoursePresenter): ViewModel


    @Binds
    @CourseScope
    internal abstract fun bindViewModelFactory(daggerViewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

}