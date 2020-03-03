package org.stepik.android.presentation.course_continue.delegate

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.ContinueLearningInteractor
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.ContinueCourseView
import ru.nobird.android.presentation.base.ViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class ContinueCoursePresenterDelegateImpl
@Inject
constructor(
    private val viewContainer: ViewContainer<out ContinueCourseView>,
    private val analytic: Analytic,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val continueLearningInteractor: ContinueLearningInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterDelegate<ContinueCourseView>(), ContinueCoursePresenterDelegate {

    override fun continueCourse(course: Course) {
        analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE)
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
                AmplitudeAnalytic.Course.Params.COURSE to course.id,
                AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.COURSE_WIDGET
            ))

        if (adaptiveCoursesResolver.isAdaptive(course.id)) {
            viewContainer.view?.showCourse(course, isAdaptive = true)
        } else {
            viewContainer.view?.setBlockingLoading(isLoading = true)
            compositeDisposable += continueLearningInteractor
                .getLastStepForCourse(course)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .doFinally { viewContainer.view?.setBlockingLoading(isLoading = false) }
                .subscribeBy(
                    onSuccess = { viewContainer.view?.showSteps(course, it) },
                    onError = { viewContainer.view?.showCourse(course, isAdaptive = false) }
                )
        }
    }
}