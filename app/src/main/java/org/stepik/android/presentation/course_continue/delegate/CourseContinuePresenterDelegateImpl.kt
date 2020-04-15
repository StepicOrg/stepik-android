package org.stepik.android.presentation.course_continue.delegate

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.ContinueLearningInteractor
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.view.injection.course_list.UserCoursesUpdateBus
import ru.nobird.android.presentation.base.ViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseContinuePresenterDelegateImpl
@Inject
constructor(
    private val viewContainer: ViewContainer<out CourseContinueView>,
    private val analytic: Analytic,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val continueLearningInteractor: ContinueLearningInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @UserCoursesUpdateBus
    private val userCoursesUpdatePublisher: PublishSubject<Course>
) : PresenterDelegate<CourseContinueView>(), CourseContinuePresenterDelegate {

    private var isBlockingLoading: Boolean = false
        set(value) {
            field = value
            viewContainer.view?.setBlockingLoading(value)
        }

    override fun attachView(view: CourseContinueView) {
        super.attachView(view)
        view.setBlockingLoading(isBlockingLoading)
    }

    override fun continueCourse(course: Course, interactionSource: CourseContinueInteractionSource) {
        userCoursesUpdatePublisher.onNext(course)
        analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE)
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
                AmplitudeAnalytic.Course.Params.COURSE to course.id,
                AmplitudeAnalytic.Course.Params.SOURCE to interactionSource
            ))

        if (adaptiveCoursesResolver.isAdaptive(course.id)) {
            viewContainer.view?.showCourse(course, isAdaptive = true)
        } else {
            isBlockingLoading = true
            compositeDisposable += continueLearningInteractor
                .getLastStepForCourse(course)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .doFinally { isBlockingLoading = false }
                .subscribeBy(
                    onSuccess = { viewContainer.view?.showSteps(course, it) },
                    onError = { viewContainer.view?.showCourse(course, isAdaptive = false) }
                )
        }
    }
}