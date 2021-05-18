package org.stepik.android.domain.step.interactor

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.concat
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.discussion_thread.repository.DiscussionThreadRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.review_instruction.model.ReviewInstructionData
import org.stepik.android.domain.review_instruction.repository.ReviewInstructionRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.model.Step
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.view.course_content.model.RequiredSection
import org.stepik.android.view.injection.step.StepDiscussionBus
import ru.nobird.android.domain.rx.maybeFirst
import javax.inject.Inject

class StepInteractor
@Inject
constructor(
    private val discussionThreadRepository: DiscussionThreadRepository,

    private val stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>,

    @StepDiscussionBus
    private val stepDiscussionObservable: Observable<Long>,
    private val stepRepository: StepRepository,
    private val stepContentResolver: StepContentResolver,

    private val reviewInstructionRepository: ReviewInstructionRepository,
    private val sectionRepository: SectionRepository,
    private val progressRepository: ProgressRepository
) {
    fun getStepUpdates(stepId: Long, shouldSkipFirstValue: Boolean = false): Observable<StepPersistentWrapper> =
        Observable
            .just(stepId)
            .concat(stepDiscussionObservable)
            .skip(if (shouldSkipFirstValue) 1 else 0)
            .filter { it == stepId }
            .flatMapMaybe { stepRepository.getStep(stepId, DataSourceType.REMOTE) }
            .flatMapSingle(stepContentResolver::resolvePersistentContent)
            .doOnNext(stepWrapperRxRelay::accept)

    fun getDiscussionThreads(step: Step): Single<List<DiscussionThread>> =
        discussionThreadRepository
            .getDiscussionThreads(*step.discussionThreads?.toTypedArray() ?: arrayOf())

    fun getReviewInstruction(instructionId: Long): Single<ReviewInstructionData> =
        reviewInstructionRepository
            .getReviewInstruction(instructionId, DataSourceType.REMOTE)

    fun getRequiredSection(sectionId: Long): Maybe<RequiredSection> =
        sectionRepository
            .getSection(sectionId, DataSourceType.CACHE)
            .flatMap { section ->
                progressRepository
                    .getProgresses(listOfNotNull(section.progress), primarySourceType = DataSourceType.CACHE)
                    .maybeFirst()
                    .map { progress ->
                        RequiredSection(section, progress)
                    }
            }
}