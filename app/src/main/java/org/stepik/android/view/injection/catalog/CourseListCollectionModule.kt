package org.stepik.android.view.injection.catalog

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.ContinueLearningInteractor
import org.stepik.android.domain.course_collection.interactor.CourseCollectionInteractor
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListCollectionView
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.presentation.course_list.CourseListQueryView
import org.stepik.android.presentation.course_list.mapper.CourseListQueryStateMapper
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.presentation.stories.StoriesViewModel
import org.stepik.android.presentation.stories.dispatcher.StoriesActionDispatcher
import org.stepik.android.presentation.stories.reducer.StoriesReducer
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
abstract class CourseListCollectionModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideCourseListCollectionPresenter(
            courseCollectionInteractor: CourseCollectionInteractor,
            courseListStateMapper: CourseListStateMapper,
            courseListInteractor: CourseListInteractor,
            @BackgroundScheduler
            backgroundScheduler: Scheduler,
            @MainScheduler
            mainScheduler: Scheduler,

            viewContainer: PresenterViewContainer<CourseListCollectionView>,

            analytic: Analytic,
            adaptiveCoursesResolver: AdaptiveCoursesResolver,
            continueLearningInteractor: ContinueLearningInteractor,
            @EnrollmentCourseUpdates
            enrollmentUpdatesObservable: Observable<Course>,
            @UserCoursesOperationBus
            userCourseOperationObservable: Observable<UserCourse>
        ): CourseListCollectionPresenter =
            CourseListCollectionPresenter(
                courseCollectionInteractor = courseCollectionInteractor,
                courseListStateMapper = courseListStateMapper,
                courseListInteractor = courseListInteractor,
                backgroundScheduler = backgroundScheduler,
                mainScheduler = mainScheduler,
                enrollmentUpdatesObservable = enrollmentUpdatesObservable,
                userCourseOperationObservable = userCourseOperationObservable,
                viewContainer = viewContainer,

                continueCoursePresenterDelegate = CourseContinuePresenterDelegateImpl(
                    viewContainer,
                    analytic,
                    adaptiveCoursesResolver,
                    continueLearningInteractor,
                    backgroundScheduler,
                    mainScheduler
                )
            )

        @Provides
        @JvmStatic
        fun provideCourseListQueryPresenter(
            courseListQueryStateMapper: CourseListQueryStateMapper,
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
            @UserCoursesOperationBus
            userCourseOperationObservable: Observable<UserCourse>
        ): CourseListQueryPresenter =
            CourseListQueryPresenter(
                courseListQueryStateMapper = courseListQueryStateMapper,
                courseListStateMapper = courseListStateMapper,
                courseListInteractor = courseListInteractor,
                backgroundScheduler = backgroundScheduler,
                mainScheduler = mainScheduler,
                enrollmentUpdatesObservable = enrollmentUpdatesObservable,
                userCourseOperationObservable = userCourseOperationObservable,
                viewContainer = viewContainer,

                continueCoursePresenterDelegate = CourseContinuePresenterDelegateImpl(
                    viewContainer,
                    analytic,
                    adaptiveCoursesResolver,
                    continueLearningInteractor,
                    backgroundScheduler,
                    mainScheduler
                )
            )

        @Provides
        @JvmStatic
        internal fun provideStoriesPresenter(
            storiesReducer: StoriesReducer,
            storiesActionDispatcher: StoriesActionDispatcher
        ): StoriesViewModel =
            StoriesViewModel(
                ReduxFeature(StoriesFeature.State.Idle, storiesReducer)
                    .wrapWithActionDispatcher(storiesActionDispatcher)
                    .wrapWithViewContainer()
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