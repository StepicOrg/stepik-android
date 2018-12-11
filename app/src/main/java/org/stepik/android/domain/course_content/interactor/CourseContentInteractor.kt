package org.stepik.android.domain.course_content.interactor

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.util.getProgresses
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.view.course_content.mapper.CourseContentItemMapper
import org.stepik.android.view.course_content.model.CourseContentItem
import javax.inject.Inject

class CourseContentInteractor
@Inject
constructor(
    private val courseObservableSource: BehaviorSubject<Course>,
    private val sectionRepository: SectionRepository,
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,

    private val courseContentItemMapper: CourseContentItemMapper
) {
    fun getCourseContent(): Observable<List<CourseContentItem>> =
        courseObservableSource
            .switchMap { course ->
                getSectionsOfCourse(course)
                    .flatMap(::populateSections)
                    .flatMapObservable { items ->
                        Single
                            .concat(Single.just(items), loadUnits(items))
                            .toObservable()
                    }
            }

    private fun getSectionsOfCourse(course: Course): Single<List<Section>> =
        sectionRepository
            .getSections(*course.sections ?: longArrayOf(), primarySourceType = DataSourceType.REMOTE)

    private fun populateSections(sections: List<Section>): Single<List<CourseContentItem>> =
        progressRepository
            .getProgresses(*sections.getProgresses())
            .map { progresses ->
                courseContentItemMapper.mapSectionsWithEmptyUnits(sections, progresses)
            }

    private fun loadUnits(items: List<CourseContentItem>): Single<List<CourseContentItem>> =
        Single
            .just(courseContentItemMapper.getUnitPlaceholdersIds(items))
            .flatMap(::getUnits)
            .flatMap { units ->
                val sections = items
                    .filterIsInstance<CourseContentItem.SectionItem>()
                    .map(CourseContentItem.SectionItem::section)

                populateUnits(sections, units)
            }
            .map { unitItems ->
                courseContentItemMapper.replaceUnitPlaceholders(items, unitItems)
            }

    private fun getUnits(unitIds: LongArray): Single<List<Unit>> =
        unitRepository
            .getUnits(*unitIds, primarySourceType = DataSourceType.REMOTE)

    private fun populateUnits(sections: List<Section>, units: List<Unit>): Single<List<CourseContentItem.UnitItem>> =
        zip(
            progressRepository.getProgresses(*units.getProgresses()),
            lessonRepository.getLessons(*units.map(Unit::lesson).toLongArray(), primarySourceType = DataSourceType.REMOTE)
        )
            .map { (progresses, lessons) ->
                courseContentItemMapper.mapUnits(units, sections, progresses, lessons)
            }

}