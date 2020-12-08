package org.stepik.android.presentation.course_continue_redux.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.ContinueLearningInteractor
import org.stepik.android.presentation.course_continue_redux.CourseContinueFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseContinueActionDispatcher
@Inject
constructor(
    private val analytic: Analytic,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val continueLearningInteractor: ContinueLearningInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseContinueFeature.Action, CourseContinueFeature.Message>() {
    override fun handleAction(action: CourseContinueFeature.Action) {
        when (action) {
            is CourseContinueFeature.Action.ContinueCourse -> {
                analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE)
                analytic.reportAmplitudeEvent(
                    AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
                        AmplitudeAnalytic.Course.Params.COURSE to action.course.id,
                        AmplitudeAnalytic.Course.Params.SOURCE to action.interactionSource
                    ))
                if (adaptiveCoursesResolver.isAdaptive(action.course.id)) {
                    onNewMessage(CourseContinueFeature.Message.ShowCourseContinue(action.course, action.viewSource, isAdaptive = true))
                    return
                } else {
                    compositeDisposable += continueLearningInteractor
                        .getLastStepForCourse(action.course)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                            onSuccess = { onNewMessage(CourseContinueFeature.Message.ShowStepsContinue(action.course, action.viewSource, it)) },
                            onError = { onNewMessage(CourseContinueFeature.Message.ShowCourseContinue(action.course, action.viewSource, isAdaptive = false)) }
                        )
                }
            }
        }
    }
}