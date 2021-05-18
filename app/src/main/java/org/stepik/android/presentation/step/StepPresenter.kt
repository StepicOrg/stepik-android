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
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.interactor.StepInteractor
import org.stepik.android.domain.step.interactor.StepNavigationInteractor
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Section
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.step.mapper.NavigationActionMapper
import org.stepik.android.view.step.model.StepNavigationAction
import timber.log.Timber
import javax.inject.Inject

class StepPresenter
@Inject
constructor(
    private val stepInteractor: StepInteractor,
    private val stepNavigationInteractor: StepNavigationInteractor,

    private val stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>,
    private val navigationActionMapper: NavigationActionMapper,

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
            .getLessonDataForDirection(stepNavigationDirection, state.stepWrapper.step, state.lessonData)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doOnSubscribe { isBlockingLoading = true }
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onSuccess = {
                    Timber.d("Current lesson: ${state.lessonData.lesson.title}")
                    Timber.d("Next lesson: ${it.lesson.title}")
                    when {
                        state.lessonData.isDemo && !it.isDemo ->
                            view?.handleNavigationAction(
                                navigationActionMapper.mapToCoursePurchaseAction(
                                    state.lessonData.course
                                )
                            )

                        it.section?.isRequirementSatisfied == false ->
                            resolveRequiredSection(state.lessonData.section, it.section)

                        // TODO Exam check will be modified in APPS-3299
                        //  to handle state when the exam has been passed.
                        it.section?.isExam == true ->
                            view?.handleNavigationAction(
                                navigationActionMapper.mapToRequiresExamAction(
                                state.lessonData.section, it.section
                            ))

                        it.section?.beginDate != null && DateTimeHelper.nowUtc() < it.section.beginDate?.time!! ->
                            view?.handleNavigationAction(
                                navigationActionMapper.mapToRequiresDateAction(
                                    state.lessonData.section,
                                    it.lesson,
                                    it.section.beginDate!!
                                )
                            )

                        else -> {
                            val action = navigationActionMapper.mapToShowLessonAction(
                                stepNavigationDirection,
                                lessonData = it,
                                isAutoplayEnabled = isAutoplayEnabled
                            )
                            view?.handleNavigationAction(action)
                        }
                    }
                },
                onError = emptyOnErrorStub
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

    private fun resolveRequiredSection(currentSection: Section?, targetSection: Section) {
        if (currentSection == null) {
            view?.handleNavigationAction(StepNavigationAction.Unknown)
            return
        }
        compositeDisposable += stepInteractor
            .getRequiredSection(targetSection.requiredSection)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doOnSubscribe { isBlockingLoading = true }
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onSuccess = { (requiredSection, requiredProgress) ->
                    val action = navigationActionMapper.mapToRequiredSectionAction(
                        currentSection,
                        targetSection,
                        requiredSection,
                        requiredProgress
                    )
                    view?.handleNavigationAction(action)
                },
                onError = { it.printStackTrace() }
            )
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