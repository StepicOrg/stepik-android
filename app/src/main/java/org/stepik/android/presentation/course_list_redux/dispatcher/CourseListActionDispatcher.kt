package org.stepik.android.presentation.course_list_redux.dispatcher

import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_recommendations.interactor.CourseRecommendationsInteractor
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.domain.rx.first
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseListActionDispatcher
@Inject
constructor(
    private val courseRecommendationsInteractor: CourseRecommendationsInteractor,
    private val courseListInteractor: CourseListInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseListFeature.Action, CourseListFeature.Message>() {
    override fun handleAction(action: CourseListFeature.Action) {
        when (action) {
            is CourseListFeature.Action.FetchCourseList -> {
                compositeDisposable += Flowable
                    .fromArray(SourceTypeComposition.CACHE, SourceTypeComposition.REMOTE)
                    .concatMapSingle { sourceType ->
                        courseListInteractor
                            .getCourseListItems(
                                action.courseIds,
                                courseViewSource = action.courseViewSource,
                                sourceTypeComposition = sourceType
                            )
                            .map { items ->
                                CourseListFeature.Message.FetchCourseListSuccess(action.id, items, items)
                            }
                    }
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onNext = { onNewMessage(it) },
                        onError = { onNewMessage(CourseListFeature.Message.FetchCourseListError(action.id)) }
                    )
            }

            is CourseListFeature.Action.FetchCourseAfterEnrollment -> {
                compositeDisposable += courseListInteractor
                    .getCourseListItems(
                        listOf(action.courseId),
                        courseViewSource = action.courseViewSource,
                        sourceTypeComposition = SourceTypeComposition.CACHE
                    )
                    .first()
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CourseListFeature.Message.OnEnrollmentFetchCourseListSuccess(action.id, it)) },
                        onError = emptyOnErrorStub
                    )
            }

            is CourseListFeature.Action.FetchCourseRecommendations -> {
                compositeDisposable += Flowable
                    .fromArray(SourceTypeComposition.CACHE, SourceTypeComposition.REMOTE)
                    .concatMapSingle { sourceTypeComposition ->
                        courseRecommendationsInteractor.fetchCourseRecommendations(sourceType = sourceTypeComposition.generalSourceType)
                            .flatMap { courseRecommendations ->
                                courseListInteractor
                                    .getCourseListItems(
                                        courseRecommendations.first().courses,
                                        CourseViewSource.Recommendation,
                                        sourceTypeComposition
                                    )
                                    .map { items ->
                                        CourseListFeature.Message.FetchCourseListSuccess(action.id, items, items)
                                    }
                            }
                    }
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onNext = { onNewMessage(it) },
                        onError = { onNewMessage(CourseListFeature.Message.FetchCourseListError(action.id)) }
                    )
                }
            }
    }
}