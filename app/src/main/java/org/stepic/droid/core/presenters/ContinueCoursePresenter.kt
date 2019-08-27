package org.stepic.droid.core.presenters

import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepik.android.model.Course
import org.stepik.android.domain.course.interactor.ContinueLearningInteractor
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ContinueCoursePresenter
@Inject
constructor(
    private val threadPoolExecutor: ThreadPoolExecutor,
    private val mainHandler: MainHandler,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val continueLearningInteractor: ContinueLearningInteractor
) : PresenterBase<ContinueCourseView>() {
    private val isHandling = AtomicBoolean(false)

    fun continueCourse(course: Course) {
        if (isHandling.compareAndSet(false, true)) {
            view?.onShowContinueCourseLoadingDialog()
            threadPoolExecutor.execute {
                try {
                    if (adaptiveCoursesResolver.isAdaptive(course.id)) {
                        mainHandler.post {
                            view?.onOpenAdaptiveCourse(course)
                        }
                        return@execute
                    }

                    val lastStep = continueLearningInteractor
                        .getLastStepForCourse(course)
                        .blockingGet()

                    mainHandler.post {
                        view?.onOpenStep(course.id, lastStep)
                    }
                } catch (exception: Exception) {
                    //connection problem || something is null -> try to resolve local
                    mainHandler.post {
                        view?.onAnyProblemWhileContinue(course)
                    }
                } finally {
                    isHandling.set(false)
                }
            }
        }
    }
}
