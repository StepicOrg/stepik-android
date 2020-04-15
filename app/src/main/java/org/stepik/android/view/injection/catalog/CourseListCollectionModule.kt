package org.stepik.android.view.injection.catalog

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.ContinueLearningInteractor
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListCollectionView
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.presentation.course_list.CourseListQueryView
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesUpdateBus
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer

@Module
abstract class CourseListCollectionModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideCourseListCollectionPresenter(
            courseListInteractor: CourseListInteractor,
            @BackgroundScheduler
            backgroundScheduler: Scheduler,
            @MainScheduler
            mainScheduler: Scheduler,

            viewContainer: PresenterViewContainer<CourseListCollectionView>,

            analytic: Analytic,
            adaptiveCoursesResolver: AdaptiveCoursesResolver,
            continueLearningInteractor: ContinueLearningInteractor,
            @UserCoursesUpdateBus
            userCoursesUpdatePublisher: PublishSubject<Course>
        ): CourseListCollectionPresenter =
            CourseListCollectionPresenter(
                courseListInteractor = courseListInteractor,
                backgroundScheduler = backgroundScheduler,
                mainScheduler = mainScheduler,
                viewContainer = viewContainer,

                continueCoursePresenterDelegate = CourseContinuePresenterDelegateImpl(
                    viewContainer,
                    analytic,
                    adaptiveCoursesResolver,
                    continueLearningInteractor,
                    backgroundScheduler,
                    mainScheduler,
                    userCoursesUpdatePublisher
                )
            )

        @Provides
        @JvmStatic
        fun provideCourseListQueryPresenter(
            courseListStateMapper: CourseListStateMapper,
            courseListInteractor: CourseListInteractor,
            @BackgroundScheduler
            backgroundScheduler: Scheduler,
            @MainScheduler
            mainScheduler: Scheduler,

            viewContainer: PresenterViewContainer<CourseListQueryView>,

            analytic: Analytic,
            adaptiveCoursesResolver: AdaptiveCoursesResolver,
            continueLearningInteractor: ContinueLearningInteractor,
            @EnrollmentCourseUpdates
            enrollmentUpdatesObservable: Observable<Course>,
            @UserCoursesUpdateBus
            userCoursesUpdatePublisher: PublishSubject<Course>
        ): CourseListQueryPresenter =
            CourseListQueryPresenter(
                courseListStateMapper = courseListStateMapper,
                courseListInteractor = courseListInteractor,
                backgroundScheduler = backgroundScheduler,
                mainScheduler = mainScheduler,
                enrollmentUpdatesObservable = enrollmentUpdatesObservable,
                viewContainer = viewContainer,

                continueCoursePresenterDelegate = CourseContinuePresenterDelegateImpl(
                    viewContainer,
                    analytic,
                    adaptiveCoursesResolver,
                    continueLearningInteractor,
                    backgroundScheduler,
                    mainScheduler,
                    userCoursesUpdatePublisher
                )
            )

        @Provides
        @JvmStatic
        fun provideCollectionViewContainer(): PresenterViewContainer<CourseListCollectionView> =
            DefaultPresenterViewContainer()

        @Provides
        @JvmStatic
        fun provideQueryViewContainer(): PresenterViewContainer<CourseListQueryView> =
            DefaultPresenterViewContainer()
    }
}