package org.stepik.android.presentation.course

import android.os.Bundle
import io.reactivex.*
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.solovyev.android.checkout.UiCheckout
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.course.interactor.*
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.notification.interactor.CourseNotificationInteractor
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.course.mapper.toEnrollmentError
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import javax.inject.Inject

class CoursePresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,

    private val courseInteractor: CourseInteractor,
    private val courseBillingInteractor: CourseBillingInteractor,
    private val courseEnrollmentInteractor: CourseEnrollmentInteractor,
    private val continueLearningInteractor: ContinueLearningInteractor,
    private val courseIndexingInteractor: CourseIndexingInteractor,

    private val courseNotificationInteractor: CourseNotificationInteractor,

    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,

    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseView>() {
    companion object {
        private const val KEY_COURSE_HEADER_DATA = "course_header_data"
    }

    private var state: CourseView.State = CourseView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
            startIndexing()
        }

    private var uiCheckout: UiCheckout? = null

    init {
        subscriberForEnrollmentUpdates()
    }

    override fun attachView(view: CourseView) {
        super.attachView(view)
        view.setState(state)
        startIndexing()

        uiCheckout = view
            .createUiCheckout()
            .also(UiCheckout::start)
    }

    override fun detachView(view: CourseView) {
        super.detachView(view)
        endIndexing()

        uiCheckout?.let(UiCheckout::stop)
        uiCheckout = null
    }

    /**
     * Data initialization variants
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (state != CourseView.State.Idle) return
        val data = savedInstanceState.getParcelable(KEY_COURSE_HEADER_DATA)
                as? CourseHeaderData ?: return
        courseInteractor.restoreCourse(data.course)
        state = CourseView.State.CourseLoaded(data)
    }

    fun onCourseId(courseId: Long, forceUpdate: Boolean = false) {
        observeCourseData(courseInteractor.getCourseHeaderData(courseId), forceUpdate)
    }

    fun onCourse(course: Course, forceUpdate: Boolean = false) {
        observeCourseData(courseInteractor.getCourseHeaderData(course), forceUpdate)
    }

    private fun observeCourseData(courseDataSource: Maybe<CourseHeaderData>, forceUpdate: Boolean) {
        if (state != CourseView.State.Idle
            && !((state == CourseView.State.NetworkError || state is CourseView.State.CourseLoaded) && forceUpdate)) return

        state = CourseView.State.Loading
        compositeDisposable += courseDataSource
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = { state = CourseView.State.EmptyCourse },
                onSuccess  = { state = CourseView.State.CourseLoaded(it); postCourseViewedNotification(it.courseId) },
                onError    = { state = CourseView.State.NetworkError }
            )
    }

    private fun postCourseViewedNotification(courseId: Long) {
        compositeDisposable += courseNotificationInteractor
            .markCourseNotificationsAsRead(courseId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(onError = emptyOnErrorStub)
    }

    /**
     * Enrollment
     */
    fun enrollCourse() {
        toggleEnrollment(CourseEnrollmentInteractor::enrollCourse)
    }

    fun dropCourse() {
        toggleEnrollment(CourseEnrollmentInteractor::dropCourse)
    }

    private inline fun toggleEnrollment(enrollmentAction: CourseEnrollmentInteractor.(Long) -> Single<Course>) {
        val headerData = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?.takeIf { it.enrollmentState != EnrollmentState.Pending }
            ?: return

        state = CourseView.State.BlockingLoading(headerData.copy(enrollmentState = EnrollmentState.Pending))
        compositeDisposable += courseEnrollmentInteractor
            .enrollmentAction(headerData.courseId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onError = {
                    val errorType = it.toEnrollmentError()
                    if (errorType == EnrollmentError.UNAUTHORIZED) {
                        view?.showEmptyAuthDialog(headerData.course)
                    } else {
                        view?.showEnrollmentError(errorType)
                    }
                    state = CourseView.State.CourseLoaded(headerData) // roll back data
                }
            )
    }

    private fun subscriberForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .filter { it.id == courseId }
            .concatMap { courseInteractor.getCourseHeaderData(it).toObservable() }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext  = { state = CourseView.State.CourseLoaded(it); continueLearning(); resolveCourseShareTooltip(it) },
                onError = { state = CourseView.State.NetworkError; subscriberForEnrollmentUpdates() }
            )
    }

    private fun resolveCourseShareTooltip(courseHeaderData: CourseHeaderData) {
        if (courseHeaderData.enrollmentState == EnrollmentState.Enrolled) {
            view?.showCourseShareTooltip()
        }
    }

    /**
     * Purchases
     */
    fun restoreCoursePurchase() {
        TODO()
    }

    fun purchaseCourse() {
        val headerData = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?.takeIf { it.enrollmentState is EnrollmentState.NotEnrolledInApp }
            ?: return

        val sku = (headerData.enrollmentState as? EnrollmentState.NotEnrolledInApp)
            ?.sku
            ?: return

        val checkout = this.uiCheckout
            ?: return

        state = CourseView.State.BlockingLoading(headerData.copy(enrollmentState = EnrollmentState.Pending))
        compositeDisposable += courseBillingInteractor
            .purchaseCourse(checkout, headerData.courseId, sku)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onError = {
                    val errorType = it.toEnrollmentError()
                    if (errorType == EnrollmentError.UNAUTHORIZED) {
                        view?.showEmptyAuthDialog(headerData.course)
                    } else {
                        view?.showEnrollmentError(errorType)
                    }
                    state = CourseView.State.CourseLoaded(headerData) // roll back data
                }
            )

    }

    fun openCoursePurchaseInWeb() {
        view?.openCoursePurchaseInWeb(courseId)
    }

    /**
     * Continue learning
     */
    fun continueLearning() {
        val headerData = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?.takeIf { it.enrollmentState == EnrollmentState.Enrolled }
            ?: return

        val course = headerData.course

        if (adaptiveCoursesResolver.isAdaptive(course.id)) {
            view?.continueAdaptiveCourse(course)
        } else {
            state = CourseView.State.BlockingLoading(headerData.copy(enrollmentState = EnrollmentState.Pending))
            compositeDisposable += continueLearningInteractor
                .getLastStepForCourse(course)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = {
                        state = CourseView.State.CourseLoaded(headerData)
                        view?.continueCourse(it)
                    },
                    onError = {
                        state = CourseView.State.CourseLoaded(headerData)
                        view?.showContinueLearningError()
                    }
                )
        }
    }

    /**
     * Indexing
     */
    private fun startIndexing() {
        (state as? CourseView.State.CourseLoaded)
            ?.takeIf { view != null }
            ?.courseHeaderData
            ?.course
            ?.let(courseIndexingInteractor::startIndexing)
    }

    private fun endIndexing() {
        courseIndexingInteractor.endIndexing()
    }

    /**
     * Sharing
     */
    fun shareCourse() {
        val course = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?.course
            ?: return

        view?.shareCourse(course)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_COURSE_HEADER_DATA, (state as? CourseView.State.CourseLoaded)?.courseHeaderData)
    }
}