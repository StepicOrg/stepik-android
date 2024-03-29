package org.stepik.android.domain.lesson.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Maybes.zip
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.discussion_thread.repository.DiscussionThreadRepository
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.lesson.model.LessonDeepLinkData
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import org.stepik.android.model.comments.DiscussionThread
import javax.inject.Inject

class LessonInteractor
@Inject
constructor(
    private val lessonRepository: LessonRepository,
    private val unitRepository: UnitRepository,
    private val sectionRepository: SectionRepository,
    private val courseRepository: CourseRepository,
    private val discussionThreadRepository: DiscussionThreadRepository
) {
    fun getLessonData(lesson: Lesson, unit: Unit, section: Section, isFromNextLesson: Boolean): Maybe<LessonData> =
        zip(
            lessonRepository.getLesson(lesson.id).onErrorReturnItem(lesson),
            unitRepository.getUnit(unit.id).onErrorReturnItem(unit),
            sectionRepository.getSection(section.id).onErrorReturnItem(section)
        )
            .flatMap { (lesson, unit, section) ->
                courseRepository
                    .getCourse(section.course)
                    .map { course ->
                        LessonData(lesson, unit, section, course, stepPosition = if (isFromNextLesson) lesson.steps.size - 1 else 0)
                    }
            }

    fun getLessonData(lastStep: LastStep): Maybe<LessonData> =
        zip(
            lessonRepository.getLesson(lastStep.lesson),
            unitRepository.getUnit(lastStep.unit)
        )
            .flatMap { (lesson, unit) ->
                sectionRepository
                    .getSection(unit.section)
                    .map { section ->
                        Triple(lesson, unit, section)
                    }
            }
            .flatMap { (lesson, unit, section) ->
                courseRepository
                    .getCourse(section.course)
                    .map { course ->
                        LessonData(lesson, unit, section, course, (lesson.steps.indexOf(lastStep.step)).coerceAtLeast(0))
                    }
            }

    fun getLessonData(lessonDeepLinkData: LessonDeepLinkData): Maybe<LessonData> =
        resolveLessonDataByLessonAndUnitId(lessonDeepLinkData.lessonId, lessonDeepLinkData.unitId ?: 0, lessonDeepLinkData.stepPosition - 1, lessonDeepLinkData.discussionId, lessonDeepLinkData.discussionThread)

    fun getLessonData(trialLessonId: Long, trialUnitId: Long): Maybe<LessonData> =
        resolveLessonDataByLessonAndUnitId(trialLessonId, trialUnitId)

    fun getDiscussionThreads(step: Step): Single<List<DiscussionThread>> =
        discussionThreadRepository
            .getDiscussionThreads(*step.discussionThreads?.toTypedArray() ?: arrayOf())

    private fun resolveLessonDataByLessonAndUnitId(
        lessonId: Long,
        unitId: Long = 0,
        stepPosition: Int = 0,
        discussionId: Long? = null,
        discussionThread: String? = null
    ): Maybe<LessonData> =
        lessonRepository
            .getLesson(lessonId)
            .flatMapSingleElement { lesson ->
                resolveUnitSource(unitId, lesson.id)
                    .flatMap { unit ->
                        sectionRepository
                            .getSection(unit.section)
                            .map { section ->
                                unit to section
                            }
                    }
                    .flatMap { (unit, section) ->
                        courseRepository
                            .getCourse(section.course)
                            .map { course ->
                                LessonData(lesson, unit, section, course, stepPosition, discussionId, discussionThread)
                            }
                    }
                    .toSingle(LessonData(lesson, null, null, null, stepPosition, discussionId, discussionThread))
            }

    private fun resolveUnitSource(unitId: Long, lessonId: Long): Maybe<Unit> =
        if (unitId == 0L) {
            unitRepository
                .getUnitsByLessonId(lessonId)
                .maybeFirst()
        } else {
            unitRepository
                .getUnit(unitId)
        }
}