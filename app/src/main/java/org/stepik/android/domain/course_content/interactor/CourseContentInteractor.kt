package org.stepik.android.domain.course_content.interactor

import io.reactivex.Observable
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.util.concat
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
            .switchMap(::getSectionsOfCourse)
            .flatMap(::populateSectionsProgresses)
            .flatMap { items ->
                Observable.just(items) concat loadUnits(items)
            }

    private fun getSectionsOfCourse(course: Course): Observable<List<Section>> =
        sectionRepository
            .getSections(*course.sections ?: longArrayOf(), primarySourceType = DataSourceType.REMOTE)
            .toObservable()

    private fun populateSectionsProgresses(sections: List<Section>): Observable<List<CourseContentItem>> =
        progressRepository
            .getProgresses(*sections.getProgresses())
            .map { progresses ->
                sections
                    .flatMap { section ->
                        courseContentItemMapper
                            .mapSectionWithEmptyUnits(section, progresses.find { it.id == section.progress })
                    }
            }
            .toObservable()

    private fun loadUnits(items: List<CourseContentItem>): Observable<List<CourseContentItem>> =
        Observable
            .just(items.filterIsInstance<CourseContentItem.UnitItemPlaceholder>())
            .map { it.map(CourseContentItem.UnitItemPlaceholder::unitId) }
            .flatMap(::getUnits)
            .flatMap { units ->
                val sections = items
                    .filterIsInstance<CourseContentItem.SectionItem>()
                    .map(CourseContentItem.SectionItem::section)

                populateUnits(sections, units)
            }
            .map { unitItems ->
                items.map { item ->
                    (item as? CourseContentItem.UnitItemPlaceholder)
                        ?.let { unitItems.find { unitItem -> it.unitId == unitItem.unit.id } }
                        ?: item
                }
            }

    private fun getUnits(unitIds: List<Long>): Observable<List<Unit>> =
        unitRepository
            .getUnits(*unitIds.toLongArray(), primarySourceType = DataSourceType.REMOTE)
            .toObservable()


    private fun populateUnits(sections: List<Section>, units: List<Unit>): Observable<List<CourseContentItem.UnitItem>> =
        zip(
            progressRepository.getProgresses(*units.getProgresses()),
            lessonRepository.getLessons(*units.map(Unit::lesson).toLongArray(), primarySourceType = DataSourceType.REMOTE)
        )
            .toObservable()
            .map { (progresses, lessons) ->
                courseContentItemMapper.mapUnits(units, sections, progresses, lessons)
            }

}