package org.stepik.android.presentation.course_content

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.personal_deadlines.model.Deadline
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepic.droid.persistence.downloads.interactor.DownloadInteractor
import org.stepic.droid.persistence.downloads.progress.DownloadProgressProvider
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.calendar.model.CalendarItem
import org.stepik.android.domain.course_calendar.interactor.CourseCalendarInteractor
import org.stepik.android.domain.course_content.interactor.CourseContentInteractor
import org.stepik.android.domain.network.exception.NetworkRequirementsNotSatisfiedException
import org.stepik.android.domain.personal_deadlines.interactor.DeadlinesInteractor
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import org.stepik.android.domain.settings.interactor.VideoQualityInteractor
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.course_calendar.model.CalendarError
import org.stepik.android.presentation.course_content.mapper.CourseContentStateMapper
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState
import org.stepik.android.view.course_content.model.CourseContentItem
import javax.inject.Inject

class CourseContentPresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,

    private val courseContentInteractor: CourseContentInteractor,

    private val sectionDownloadProgressProvider: DownloadProgressProvider<Section>,
    private val sectionDownloadInteractor: DownloadInteractor<Section>,

    private val unitDownloadProgressProvider: DownloadProgressProvider<Unit>,
    private val unitDownloadInteractor: DownloadInteractor<Unit>,

    private val videoQualityInteractor: VideoQualityInteractor,

    private val deadlinesInteractor: DeadlinesInteractor,
    private val stateMapper: CourseContentStateMapper,

    private val progressObservable: Observable<Progress>,

    private val courseCalendarInteractor: CourseCalendarInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseContentView>() {
    private var state: CourseContentView.State = CourseContentView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private var isBlockingLoading: Boolean = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    private val downloadsDisposable = CompositeDisposable()
    private val deadlinesDisposable = CompositeDisposable()

    private val pendingUnits = mutableSetOf<Long>()
    private val pendingSections = mutableSetOf<Long>()

    init {
        compositeDisposable += downloadsDisposable
        compositeDisposable += deadlinesDisposable
        fetchCourseContent()
        subscribeForProgressesUpdates()
    }

    override fun attachView(view: CourseContentView) {
        super.attachView(view)
        view.setState(state)
        view.setBlockingLoading(isBlockingLoading)
        resolveDownloadProgressSubscription()
    }

    /**
     * Content
     */
    private fun fetchCourseContent() {
        if (state != CourseContentView.State.Idle) return

        state = CourseContentView.State.Loading
        subscribeForCourseContent()
    }

    private fun subscribeForCourseContent(shouldSkipStoredValue: Boolean = false) {
        compositeDisposable += courseContentInteractor
            .getCourseContent(shouldSkipStoredValue)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { (course, courseContent) ->
                    state = stateMapper.mergeStateWithCourseContent(state, course, courseContent)
                    resolveDownloadProgressSubscription()
                    fetchPersonalDeadlines()
                },
                onError = {
                    state = CourseContentView.State.NetworkError
                    subscribeForCourseContent(shouldSkipStoredValue = true)
                }
            )
    }

    /**
     * Download progresses
     */
    private fun resolveDownloadProgressSubscription() {
        val items =
            (state as? CourseContentView.State.CourseContentLoaded)
            ?.courseContent
            ?.takeIf { view != null }
            ?: return

        downloadsDisposable.clear()
        val sectionIds = items
            .mapNotNull { item ->
                (item as? CourseContentItem.SectionItem)
                    ?.takeIf(CourseContentItem.SectionItem::isEnabled)
                    ?.section
                    ?.id
            }
            .toLongArray()

        subscribeForSectionsProgress(*sectionIds)

        val unitIds = items
            .mapNotNull {
                (it as? CourseContentItem.UnitItem)?.takeIf(CourseContentItem.UnitItem::isEnabled)?.unit?.id
                    ?: (it as? CourseContentItem.UnitItemPlaceholder)?.unitId
            }
            .toLongArray()

        subscribeForUnitsProgress(*unitIds)
    }

    private fun subscribeForSectionsProgress(vararg sectionIds: Long, limit: Long = -1) {
        downloadsDisposable += sectionDownloadProgressProvider
            .getProgress(*sectionIds)
            .let { stream ->
                limit
                    .takeUnless { it < 0 }
                    ?.let(stream::take)
                    ?: stream
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .filter { it.id !in pendingSections }
            .subscribeBy(
                onError = { it.printStackTrace(); resolveDownloadProgressSubscription() }, // resub on error
                onNext  = { view?.updateSectionDownloadProgress(it) }
            )
    }

    private fun subscribeForUnitsProgress(vararg unitIds: Long, limit: Long = -1) {
        downloadsDisposable += unitDownloadProgressProvider
            .getProgress(*unitIds)
            .let { stream ->
                limit
                    .takeUnless { it < 0 }
                    ?.let(stream::take)
                    ?: stream
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .filter { it.id !in pendingUnits }
            .subscribeBy(
                onError = { it.printStackTrace(); resolveDownloadProgressSubscription() }, // resub on error
                onNext  = { view?.updateUnitDownloadProgress(it) }
            )
    }

    /**
     * Download tasks
     */
    fun addCourseDownloadTask(course: Course, videoQuality: String? = null) {
        val quality = videoQuality
            ?: videoQualityInteractor.getVideoQuality()
            ?: return view?.showVideoQualityDialog(course = course) ?: kotlin.Unit

        (state as? CourseContentView.State.CourseContentLoaded)
            ?.courseContent
            ?.filterIsInstance<CourseContentItem.SectionItem>()
            ?.forEach { sectionItem ->
                addSectionDownloadTask(sectionItem.section, quality)
            }
    }

    fun addUnitDownloadTask(unit: Unit, videoQuality: String? = null) {
        if (unit.id in pendingUnits) return

        val quality = videoQuality
            ?: videoQualityInteractor.getVideoQuality()
            ?: return view?.showVideoQualityDialog(unit = unit) ?: kotlin.Unit

        pendingUnits += unit.id
        view?.updateUnitDownloadProgress(DownloadProgress(unit.id, DownloadProgress.Status.Pending))

        compositeDisposable += unitDownloadInteractor
            .addTask(unit, configuration = DownloadConfiguration(videoQuality = quality))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally {
                pendingUnits -= unit.id
                subscribeForUnitsProgress(unit.id, limit = 1)
            }
            .subscribeBy(
                onError = {
                    if (it is NetworkRequirementsNotSatisfiedException) {
                        view?.showChangeDownloadNetworkType()
                    }
                }
            )
    }

    fun removeUnitDownloadTask(unit: Unit) {
        if (unit.id in pendingUnits) return
        pendingUnits += unit.id
        view?.updateUnitDownloadProgress(DownloadProgress(unit.id, DownloadProgress.Status.Pending))

        compositeDisposable += unitDownloadInteractor
            .removeTask(unit)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally {
                pendingUnits -= unit.id
                subscribeForUnitsProgress(unit.id, limit = 1)
            }
            .subscribeBy(onError = emptyOnErrorStub)
    }

    fun addSectionDownloadTask(section: Section, videoQuality: String? = null) {
        if (section.id in pendingSections) return

        val quality = videoQuality
            ?: videoQualityInteractor.getVideoQuality()
            ?: return view?.showVideoQualityDialog(section = section) ?: kotlin.Unit

        pendingSections.add(section.id)
        view?.updateSectionDownloadProgress(DownloadProgress(section.id, DownloadProgress.Status.Pending))

        compositeDisposable += sectionDownloadInteractor
            .addTask(section, configuration = DownloadConfiguration(videoQuality = quality))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally {
                pendingSections -= section.id
                subscribeForSectionsProgress(section.id, limit = 1)
            }
            .subscribeBy(
                onError = {
                    if (it is NetworkRequirementsNotSatisfiedException) {
                        view?.showChangeDownloadNetworkType()
                    }
                }
            )
    }

    fun removeSectionDownloadTask(section: Section) {
        if (section.id in pendingSections) return
        pendingSections += section.id
        view?.updateSectionDownloadProgress(DownloadProgress(section.id, DownloadProgress.Status.Pending))

        compositeDisposable += sectionDownloadInteractor
            .removeTask(section)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally {
                pendingSections -= section.id
                subscribeForSectionsProgress(section.id, limit = 1)
            }
            .subscribeBy(onError = emptyOnErrorStub)
    }

    /*
     * Personal deadlines
     */
    private fun fetchPersonalDeadlines() {
        (state as? CourseContentView.State.CourseContentLoaded)
            ?.takeIf { it.personalDeadlinesState == PersonalDeadlinesState.Idle }
            ?: return

        deadlinesDisposable.clear()

        deadlinesDisposable += deadlinesInteractor
            .getPersonalDeadlineByCourseId(courseId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = { state = stateMapper.mergeStateWithPersonalDeadlines(state, null); fetchPersonalDeadlinesBanner() },
                onSuccess  = { state = stateMapper.mergeStateWithPersonalDeadlines(state, it) },
                onError    = { it.printStackTrace(); view?.showPersonalDeadlinesError() }
            )
    }

    fun createPersonalDeadlines(learningRate: LearningRate) {
        (state as? CourseContentView.State.CourseContentLoaded)
            ?.takeIf { it.personalDeadlinesState == PersonalDeadlinesState.EmptyDeadlines }
            ?: return

        isBlockingLoading = true
        deadlinesDisposable += deadlinesInteractor
            .createPersonalDeadlines(courseId, learningRate)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onSuccess = { state = stateMapper.mergeStateWithPersonalDeadlines(state, it) },
                onError   = { view?.showPersonalDeadlinesError() }
            )
    }

    fun updatePersonalDeadlines(deadlines: List<Deadline>) {
        val oldState =
            (state as? CourseContentView.State.CourseContentLoaded)
            ?: return

        val record =
            (oldState.personalDeadlinesState as? PersonalDeadlinesState.Deadlines)
            ?.record
            ?: return

        val newRecord = record.copy(data = DeadlinesWrapper(record.data.course, deadlines))

        isBlockingLoading = true
        deadlinesDisposable += deadlinesInteractor
            .updatePersonalDeadlines(newRecord)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onSuccess = { state = stateMapper.mergeStateWithPersonalDeadlines(state, it) },
                onError   = { view?.showPersonalDeadlinesError() }
            )
    }

    fun removeDeadlines() {
        val oldState =
            (state as? CourseContentView.State.CourseContentLoaded)
            ?: return

        val recordId =
            (oldState.personalDeadlinesState as? PersonalDeadlinesState.Deadlines)
            ?.record
            ?.id
            ?: return

        isBlockingLoading = true
        deadlinesDisposable += deadlinesInteractor
            .removePersonalDeadline(recordId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onComplete = { state = stateMapper.mergeStateWithPersonalDeadlines(state, null) },
                onError    = { view?.showPersonalDeadlinesError() }
            )
    }

    private fun fetchPersonalDeadlinesBanner() {
        (state as? CourseContentView.State.CourseContentLoaded)
            ?.takeIf { it.personalDeadlinesState == PersonalDeadlinesState.EmptyDeadlines }
            ?: return

        deadlinesDisposable += deadlinesInteractor
            .shouldShowDeadlinesBannerForCourse(courseId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { wasDeadlinesBannerShown ->
                    if (!wasDeadlinesBannerShown) {
                        view?.showPersonalDeadlinesBanner()
                    }
                },
                onError   = { view?.showPersonalDeadlinesError() }
            )
    }

    /**
     * Progresses
     */
    private fun subscribeForProgressesUpdates() {
        compositeDisposable += progressObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { progress ->
                    val newState = stateMapper.mergeStateWithProgress(state, progress)
                    if (state !== newState) { // compare by reference
                        state = newState
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    fun getCalendarPrimaryItems() {
        isBlockingLoading = true
        compositeDisposable += courseCalendarInteractor
                .getCalendarItems()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .doFinally { isBlockingLoading = false }
                .subscribeBy(
                    onSuccess = {
                        if (it.isEmpty()) {
                            view?.showCalendarError(CalendarError.NO_CALENDARS_ERROR)
                        }
                        else {
                            view?.showCalendarChoiceDialog(it)
                        }
                    },
                    onError = { view?.showCalendarError(CalendarError.GENERIC_ERROR) }
                )
    }

    fun exportScheduleToCalendar(calendarItem: CalendarItem) {
        val items = (state as? CourseContentView.State.CourseContentLoaded)
                ?.courseContent
                ?: return

        compositeDisposable += courseCalendarInteractor
                .exportScheduleToCalendar(items, calendarItem)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = { view?.showCalendarSyncSuccess() },
                    onError = { view?.showCalendarError(CalendarError.GENERIC_ERROR) }
                )
    }

    override fun detachView(view: CourseContentView) {
        downloadsDisposable.clear()
        super.detachView(view)
    }
}