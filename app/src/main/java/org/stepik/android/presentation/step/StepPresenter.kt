package org.stepik.android.presentation.step

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.exam.model.ExamStatus
import org.stepik.android.domain.exam.model.SessionData
import org.stepik.android.domain.exam.resolver.ExamStatusResolver
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.interactor.StepInteractor
import org.stepik.android.domain.step.interactor.StepNavigationInteractor
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.course_content.model.RequiredSection
import org.stepik.android.presentation.step.mapper.NavigationActionMapper
import org.stepik.android.view.step.model.StepNavigationAction
import javax.inject.Inject

class StepPresenter
@Inject
constructor(
    private val stepInteractor: StepInteractor,
    private val stepNavigationInteractor: StepNavigationInteractor,

    private val stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>,
    private val navigationActionMapper: NavigationActionMapper,
    private val examStatusResolver: ExamStatusResolver,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StepView>() {
    private var state: StepView.State = StepView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private var stepNavigationDirections: Set<StepNavigationDirection> = emptySet()
        set(value) {
            field = value
            view?.setNavigation(value)
        }

    private var isBlockingLoading: Boolean = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    private val stepUpdatesDisposable = CompositeDisposable()

    init {
        compositeDisposable += stepUpdatesDisposable
        subscribeForStepWrapperRelay()
    }

    override fun attachView(view: StepView) {
        super.attachView(view)
        view.setBlockingLoading(isBlockingLoading)
        view.setNavigation(stepNavigationDirections)
        view.setState(state)
    }

    fun onLessonData(stepWrapper: StepPersistentWrapper, lessonData: LessonData) {
        if (state != StepView.State.Idle) return

        val discussionThreads =
            stepWrapper.step.discussionProxy
                ?.let {
                    listOf(DiscussionThread(
                        stepWrapper.step.discussionThreads?.firstOrNull() ?: "",
                        DiscussionThread.THREAD_DEFAULT,
                        discussionsCount = stepWrapper.step.discussionsCount,
                        discussionProxy = it
                    ))
                }
                ?: emptyList()
        state = StepView.State.Loaded(stepWrapper, lessonData, discussionThreads)

        fetchDiscussionThreads(stepWrapper)
        fetchNavigation(stepWrapper, lessonData)
        subscribeForStepUpdates(stepWrapper.step.id)
    }

    /**
     * Step updates
     */
    private fun subscribeForStepUpdates(stepId: Long, shouldSkipFirstValue: Boolean = false) {
        stepUpdatesDisposable.clear()

        stepUpdatesDisposable += stepInteractor
            .getStepUpdates(stepId, shouldSkipFirstValue)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { stepWrapper ->
                    val oldState = this.state
                    if (oldState is StepView.State.Loaded) {
                        this.state = oldState.copy(stepWrapper)
                        fetchDiscussionThreads(stepWrapper)
                    }
                },
                onError = { subscribeForStepUpdates(stepId, shouldSkipFirstValue = true) }
            )
    }

    private fun subscribeForStepWrapperRelay() {
        compositeDisposable += stepWrapperRxRelay
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { stepWrapper ->
                    val oldState = this.state
                    if (oldState is StepView.State.Loaded &&
                        oldState.stepWrapper.step.block != stepWrapper.step.block) {
                        if (stepWrapper.isStepCanHaveQuiz) {
                            view?.showQuizReloadMessage()
                        }
                        this.state = oldState.copy(stepWrapper)
                    }
                }
            )
    }

    fun fetchStepUpdate(stepId: Long) {
        subscribeForStepUpdates(stepId, false)
    }

    /**
     * Navigation
     */
    private fun fetchNavigation(stepWrapper: StepPersistentWrapper, lessonData: LessonData) {
        compositeDisposable += stepNavigationInteractor
            .getStepNavigationDirections(stepWrapper.step, lessonData)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { stepNavigationDirections = it },
                onError = emptyOnErrorStub
            )
    }

    fun onStepDirectionClicked(stepNavigationDirection: StepNavigationDirection, isAutoplayEnabled: Boolean = false) {
        val state = (state as? StepView.State.Loaded)
            ?: return

        compositeDisposable += stepNavigationInteractor
            .getStepDirectionData(stepNavigationDirection, state.stepWrapper.step, state.lessonData)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doOnSubscribe { isBlockingLoading = true }
            .doFinally { isBlockingLoading = false }
            .map { stepDirectionData ->
                val requiredSection = stepDirectionData.requiredSection.takeIf { it != RequiredSection.EMPTY }
                val sessionData = stepDirectionData.examSessionData.takeIf { it != SessionData.EMPTY }
                mapToStepNavigationAction(stepNavigationDirection, state.lessonData, stepDirectionData.lessonData, requiredSection, sessionData, isAutoplayEnabled)
            }
            .subscribeBy(
                onSuccess = { view?.handleNavigationAction(it) },
                onError = emptyOnErrorStub,
                onComplete = {
                    val action = navigationActionMapper.mapToCourseCompleteAction(state.lessonData.course)
                    view?.handleNavigationAction(action)
                }
            )
    }

    fun onFetchReviewInstruction(instructionId: Long) {
        if (state !is StepView.State.Loaded) return

        compositeDisposable += stepInteractor
            .getReviewInstruction(instructionId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doOnSubscribe { isBlockingLoading = true }
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onSuccess = { view?.openShowSubmissionsWithReview(it) },
                onError = { it.printStackTrace() }
            )
    }

    private fun mapToStepNavigationAction(
        stepNavigationDirection: StepNavigationDirection,
        currentLessonData: LessonData,
        targetLessonData: LessonData,
        requiredSection: RequiredSection?,
        sessionData: SessionData?,
        isAutoplayEnabled: Boolean
    ): StepNavigationAction =
        when {
            currentLessonData.isDemo && !targetLessonData.isDemo ->
                navigationActionMapper.mapToCoursePurchaseAction(currentLessonData.course)

            targetLessonData.section?.isExam == true && sessionData != null -> {
                when (examStatusResolver.resolveExamStatus(targetLessonData.section, sessionData.examSession, sessionData.proctorSession)) {
                    ExamStatus.FINISHED ->
                        navigationActionMapper.mapToShowLessonAction(stepNavigationDirection, lessonData = targetLessonData, isAutoplayEnabled = isAutoplayEnabled)
                    else ->
                        navigationActionMapper.mapToRequiresExamAction(currentLessonData.section, targetLessonData.section, requiredSection)
                }
            }

            targetLessonData.section?.isRequirementSatisfied == false ->
                navigationActionMapper.mapToRequiredSectionAction(currentLessonData.section, targetLessonData.section, requiredSection)

            targetLessonData.section?.beginDate != null && DateTimeHelper.nowUtc() < targetLessonData.section.beginDate?.time!! ->
                navigationActionMapper.mapToRequiresDateAction(currentLessonData.section, targetLessonData.lesson, targetLessonData.section.beginDate!!)

            else ->
                navigationActionMapper.mapToShowLessonAction(stepNavigationDirection, lessonData = targetLessonData, isAutoplayEnabled = isAutoplayEnabled)
        }

    /**
     * Discussions
     */
    private fun fetchDiscussionThreads(stepWrapper: StepPersistentWrapper) {
        compositeDisposable += stepInteractor
            .getDiscussionThreads(stepWrapper.step)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { discussionThreads ->
                    val oldState = (state as? StepView.State.Loaded)
                        ?: return@subscribeBy

                    state = oldState.copy(discussionThreads = discussionThreads)
                },
                onError = emptyOnErrorStub
            )
    }
}