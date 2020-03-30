package org.stepik.android.view.injection.course_list

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.presentation.course_list.CourseListSearchPresenter
import org.stepik.android.presentation.course_list.CourseListUserPresenter
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListModule {
    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(CourseListPresenter::class)
    internal abstract fun bindCourseListPresenter(courseListPresenter: CourseListPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CourseListUserPresenter::class)
    internal abstract fun bindCourseListUserPresenter(courseListUserPresenter: CourseListUserPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CourseListSearchPresenter::class)
    internal abstract fun bindCourseListSearchPresenter(courseListSearchPresenter: CourseListSearchPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CourseListCollectionPresenter::class)
    internal abstract fun bindCourseListCollectionPresenter(courseListCollectionPresenter: CourseListCollectionPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CourseListScope viewContainer: PresenterViewContainer<CourseListView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseListScope
        fun provideViewContainer(): PresenterViewContainer<CourseListView> =
            DefaultPresenterViewContainer()

        @Provides
        @JvmStatic
        @CourseListScope
        internal fun provideCourseBehaviorSubject(): BehaviorSubject<Course> =
            BehaviorSubject.create()

        @Provides
        @JvmStatic
        @CourseListScope
        internal fun provideCourseObservableSource(courseSubject: BehaviorSubject<Course>, @BackgroundScheduler scheduler: Scheduler): Observable<Course> =
            courseSubject.observeOn(scheduler)
    }
}