package org.stepik.android.domain.course_content.interactor

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.util.getProgresses
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.presentation.course_content.mapper.CourseContentItemMapper
import org.stepik.android.view.course_content.model.CourseContentItem
import javax.inject.Inject

class CourseContentInteractor
@Inject
constructor(
    private val courseObservableSource: Observable<Course>,
    private val sectionRepository: SectionRepository,
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,

    private val courseContentItemMapper: CourseContentItemMapper
) {
    fun getCourseContent(): Observable<Pair<Course, List<CourseContentItem>>> =
        courseObservableSource
            .switchMap { course ->
                getSectionsOfCourse(course)
                    .flatMap { populateSections(course, it) }
                    .flatMapObservable { items ->
                        Single
                            .concat(Single.just(course to items), loadUnits(course, items))
                            .toObservable()
                    }
            }

    private fun getSectionsOfCourse(course: Course): Single<List<Section>> =
        sectionRepository
            .getSections(*course.sections ?: longArrayOf(), primarySourceType = DataSourceType.REMOTE)

    private fun populateSections(course: Course, sections: List<Section>): Single<List<CourseContentItem>> =
        progressRepository
            .getProgresses(*sections.getProgresses())
            .map { progresses ->
                courseContentItemMapper.mapSectionsWithEmptyUnits(course, sections, progresses)
            }

    private fun loadUnits(course: Course, items: List<CourseContentItem>): Single<Pair<Course, List<CourseContentItem>>> =
        Single
            .just(courseContentItemMapper.getUnitPlaceholdersIds(items))
            .flatMap(::getUnits)
            .flatMap { units ->
                val sectionItems = items
                    .filterIsInstance<CourseContentItem.SectionItem>()

                populateUnits(sectionItems, units)
            }
            .map { unitItems ->
                course to courseContentItemMapper.replaceUnitPlaceholders(items, unitItems)
            }

    private fun getUnits(unitIds: LongArray): Single<List<Unit>> =
        unitRepository
            .getUnits(*unitIds, primarySourceType = DataSourceType.REMOTE)

    private fun populateUnits(sectionItems: List<CourseContentItem.SectionItem>, units: List<Unit>): Single<List<CourseContentItem.UnitItem>> =
        zip(
            progressRepository
                .getProgresses(*units.getProgresses()),
            lessonRepository
                .getLessons(*units.map(Unit::lesson).toLongArray(), primarySourceType = DataSourceType.REMOTE)
        )
            .map { (progresses, lessons) ->
                courseContentItemMapper.mapUnits(sectionItems, units, lessons, progresses)
            }

}