package org.stepik.android.presentation.course

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.plus
import org.stepik.android.domain.course.interactor.CourseEnrollmentInteractor
import org.stepik.android.domain.course.interactor.CourseIndexingInteractor
import org.stepik.android.domain.course.interactor.CourseInteractor
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.notification.interactor.CourseNotificationInteractor
import org.stepik.android.domain.solutions.interactor.SolutionsInteractor
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.user_courses.model.UserCourseHeader
import org.stepik.android.model.Course
import org.stepik.android.presentation.course.mapper.toEnrollmentError
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.user_courses.model.UserCourseAction
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.solutions.SolutionsBus
import org.stepik.android.view.injection.solutions.SolutionsSentBus
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoursePresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,

    viewContainer: PresenterViewContainer<CourseView>,

    private val courseContinuePresenterDelegateImpl: CourseContinuePresenterDelegateImpl,

    private val courseInteractor: CourseInteractor,
//    private val courseBillingInteractor: CourseBillingInteractor,
    private val courseEnrollmentInteractor: CourseEnrollmentInteractor,
    private val courseIndexingInteractor: CourseIndexingInteractor,
    private val solutionsInteractor: SolutionsInteractor,

    private val courseNotificationInteractor: CourseNotificationInteractor,

    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,

    @SolutionsBus
    private val solutionsObservable: Observable<Unit>,

    @SolutionsSentBus
    private val solutionsSentObservable: Observable<Unit>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val analytic: Analytic
) : PresenterBase<CourseView>(viewContainer), CourseContinuePresenterDelegate by courseContinuePresenterDelegateImpl {
    private var state: CourseView.State = CourseView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
            startIndexing()
        }

//    private var uiCheckout: UiCheckout? = null

    private var isCoursePreviewLogged = false

    override val delegates: List<PresenterDelegate<in CourseView>> =
        listOf(courseContinuePresenterDelegateImpl)

    private val userCourseDisposable = CompositeDisposable()

    init {
        compositeDisposable += userCourseDisposable
        subscriberForEnrollmentUpdates()
        subscribeForLocalSubmissionsUpdates()
    }

    override fun attachView(view: CourseView) {
        super.attachView(view)
        view.setState(state)
        startIndexing()

//        uiCheckout = view
//            .createUiCheckout()
//            .also(UiCheckout::start)
    }

    override fun detachView(view: CourseView) {
        super.detachView(view)
        endIndexing()

//        uiCheckout?.let(UiCheckout::stop)
//        uiCheckout = null
    }

    /**
     * Data initialization variants
     */

    fun onCourseId(courseId: Long, forceUpdate: Boolean = false) {
        observeCourseData(courseInteractor.getCourseHeaderData(courseId), forceUpdate)
    }

    fun onCourse(course: Course, forceUpdate: Boolean = false) {
        observeCourseData(courseInteractor.getCourseHeaderData(course), forceUpdate)
    }

    private fun observeCourseData(courseDataSource: Maybe<CourseHeaderData>, forceUpdate: Boolean) {
        if (state != CourseView.State.Idle &&
            !((state == CourseView.State.NetworkError || state is CourseView.State.CourseLoaded) && forceUpdate)
        ) {
            return
        }

        state = CourseView.State.Loading
        compositeDisposable += courseDataSource
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = { state = CourseView.State.EmptyCourse },
                onSuccess  = {
                    state = CourseView.State.CourseLoaded(it)
                    postCourseViewedNotification(it.courseId)
                    logCoursePreviewOpenedEvent(it.course)
                },
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
    fun autoEnroll() {
        val enrollmentState = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?.stats
            ?.enrollmentState
            ?: return

        when (enrollmentState) {
            EnrollmentState.NotEnrolledFree ->
                enrollCourse()

            EnrollmentState.NotEnrolledWeb ->
                openCoursePurchaseInWeb()

//            is EnrollmentState.NotEnrolledInApp ->
//                purchaseCourse()
        }
    }

    fun enrollCourse() {
        toggleEnrollment(CourseEnrollmentInteractor::enrollCourse)
    }

    fun dropCourse() {
        toggleEnrollment(CourseEnrollmentInteractor::dropCourse)
    }

    private inline fun toggleEnrollment(enrollmentAction: CourseEnrollmentInteractor.(Long) -> Single<Course>) {
        val headerData = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?.takeIf { it.stats.enrollmentState != EnrollmentState.Pending }
            ?: return

        state = CourseView.State.BlockingLoading(
            headerData.copy(
                stats = headerData.stats.copy(
                    enrollmentState = EnrollmentState.Pending
                )
            )
        )

        userCourseDisposable.clear()

        compositeDisposable += courseEnrollmentInteractor
            .enrollmentAction(headerData.courseId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onError = {
                    state = CourseView.State.CourseLoaded(headerData) // roll back data

                    val errorType = it.toEnrollmentError()
                    if (errorType == EnrollmentError.UNAUTHORIZED) {
                        view?.showEmptyAuthDialog(headerData.course)
                    } else {
                        view?.showEnrollmentError(errorType)
                    }
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

    private fun subscribeForLocalSubmissionsUpdates() {
        compositeDisposable += (solutionsObservable + solutionsSentObservable)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { updateLocalSubmissionsCount() },
                onError = emptyOnErrorStub
            )
    }

    private fun updateLocalSubmissionsCount() {
        compositeDisposable += solutionsInteractor
            .fetchAttemptCacheItems(courseId, localOnly = true)
            .map { localSubmissions -> localSubmissions.count { it is SolutionItem.SubmissionItem } }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { localSubmissionsCount ->
                    val oldState =
                        (state as? CourseView.State.CourseLoaded)
                        ?: return@subscribeBy

                    val courseHeaderData = oldState
                        .courseHeaderData
                        .copy(localSubmissionsCount = localSubmissionsCount)
                    state = CourseView.State.CourseLoaded(courseHeaderData)
                },
                onError = emptyOnErrorStub
            )
    }

    private fun resolveCourseShareTooltip(courseHeaderData: CourseHeaderData) {
        if (courseHeaderData.stats.enrollmentState == EnrollmentState.Enrolled) {
            view?.showCourseShareTooltip()
        }
    }

    /**
     * Purchases
     */
//    fun restoreCoursePurchase() {
//        val headerData = (state as? CourseView.State.CourseLoaded)
//            ?.courseHeaderData
//            ?: return
//
//        val sku = (headerData.enrollmentState as? EnrollmentState.NotEnrolledInApp)
//            ?.skuWrapper
//            ?.sku
//            ?: return
//
//        state = CourseView.State.BlockingLoading(headerData.copy(enrollmentState = EnrollmentState.Pending))
//        compositeDisposable += courseBillingInteractor
//            .restorePurchase(sku)
//            .observeOn(mainScheduler)
//            .subscribeOn(backgroundScheduler)
//            .subscribeBy(
//                onError = {
//                    state = CourseView.State.CourseLoaded(headerData) // roll back data
//
//                    when (val errorType = it.toEnrollmentError()) {
//                        EnrollmentError.UNAUTHORIZED ->
//                            view?.showEmptyAuthDialog(headerData.course)
//
//                        EnrollmentError.COURSE_ALREADY_OWNED ->
//                            enrollCourse() // try to enroll course normally
//
//                        else ->
//                            view?.showEnrollmentError(errorType)
//                    }
//                }
//            )
//    }

//    fun purchaseCourse() {
//        val headerData = (state as? CourseView.State.CourseLoaded)
//            ?.courseHeaderData
//            ?: return
//
//        val sku = (headerData.enrollmentState as? EnrollmentState.NotEnrolledInApp)
//            ?.skuWrapper
//            ?.sku
//            ?: return
//
//        val checkout = this.uiCheckout
//            ?: return
//
//        state = CourseView.State.BlockingLoading(headerData.copy(enrollmentState = EnrollmentState.Pending))
//        compositeDisposable += courseBillingInteractor
//            .purchaseCourse(checkout, headerData.courseId, sku)
//            .observeOn(mainScheduler)
//            .subscribeOn(backgroundScheduler)
//            .subscribeBy(
//                onError = {
//                    state = CourseView.State.CourseLoaded(headerData) // roll back data
//
//                    val errorType = it.toEnrollmentError()
//                    if (errorType == EnrollmentError.UNAUTHORIZED) {
//                        view?.showEmptyAuthDialog(headerData.course)
//                    } else {
//                        view?.showEnrollmentError(errorType)
//                    }
//                }
//            )
//    }

    fun openCoursePurchaseInWeb(queryParams: Map<String, List<String>>? = null) {
        view?.openCoursePurchaseInWeb(courseId, queryParams)
    }

    /**
     * Continue learning
     */
    fun continueLearning() {
        val headerData = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?.takeIf { it.stats.enrollmentState == EnrollmentState.Enrolled }
            ?: return

        courseContinuePresenterDelegateImpl.continueCourse(headerData.course, CourseContinueInteractionSource.COURSE_SCREEN)
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

    /**
     * User course operations
     */

    fun toggleUserCourse(userCourseAction: UserCourseAction) {
        val oldCourseHeaderData = (state as? CourseView.State.CourseLoaded)
            ?.courseHeaderData
            ?: return

        val oldUserCourseHeader = (oldCourseHeaderData.userCourseHeader as? UserCourseHeader.Data)
            ?: return

        val userCourse =
            when (userCourseAction) {
                UserCourseAction.ADD_ARCHIVE ->
                    oldUserCourseHeader.userCourse.copy(isArchived = true)

                UserCourseAction.REMOVE_ARCHIVE ->
                    oldUserCourseHeader.userCourse.copy(isArchived = false)

                UserCourseAction.ADD_FAVORITE ->
                    oldUserCourseHeader.userCourse.copy(isFavorite = true)

                UserCourseAction.REMOVE_FAVORITE ->
                    oldUserCourseHeader.userCourse.copy(isFavorite = false)
            }

        val newCourseHeaderData = oldCourseHeaderData.copy(userCourseHeader = oldUserCourseHeader.copy(isSending = true))
        saveUserCourse(newCourseHeaderData, userCourse, userCourseAction)
    }

    private fun saveUserCourse(preparedCourseHeaderData: CourseHeaderData, userCourse: UserCourse, userCourseAction: UserCourseAction) {
        state = CourseView.State.CourseLoaded(preparedCourseHeaderData)

        userCourseDisposable += courseInteractor
            .saveUserCourse(userCourse = userCourse)
            .delay(5, TimeUnit.SECONDS)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    val oldState = (state as? CourseView.State.CourseLoaded)
                        ?: return@subscribeBy

                    val courseHeaderData = oldState
                        .courseHeaderData
                        .copy(userCourseHeader = UserCourseHeader.Data(userCourse = it, isSending = false))

                    state = CourseView.State.CourseLoaded(courseHeaderData)
                    view?.showSaveUserCourseSuccess(userCourseAction)
                },
                onError = {
                    val oldState = (state as? CourseView.State.CourseLoaded)
                        ?: return@subscribeBy

                    val userCourseHeader = (oldState.courseHeaderData.userCourseHeader as? UserCourseHeader.Data)
                        ?: return@subscribeBy

                    val courseHeaderData = oldState
                        .courseHeaderData
                        .copy(userCourseHeader = userCourseHeader.copy(isSending = false))

                    state = CourseView.State.CourseLoaded(courseHeaderData)
                    view?.showSaveUserCourseError(userCourseAction)
                }
            )
    }

    /**
     * Analytics
     */
    private fun logCoursePreviewOpenedEvent(course: Course) {
        if (isCoursePreviewLogged) {
            return
        }
        isCoursePreviewLogged = true
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.CoursePreview.COURSE_PREVIEW_SCREEN_OPENED, mapOf(
            AmplitudeAnalytic.CoursePreview.Params.COURSE to course.id,
            AmplitudeAnalytic.CoursePreview.Params.TITLE to course.title,
            AmplitudeAnalytic.CoursePreview.Params.IS_PAID to course.isPaid
        ))
    }
}