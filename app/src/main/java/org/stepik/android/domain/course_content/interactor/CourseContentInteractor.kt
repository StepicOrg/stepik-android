package org.stepik.android.domain.course_content.interactor

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.analytic.experiments.PersonalDeadlinesSplitTest
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.concat
import org.stepic.droid.util.getProgresses
import org.stepic.droid.util.mapToLongArray
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

    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val personalDeadlinesSplitTest: PersonalDeadlinesSplitTest,

    private val courseContentItemMapper: CourseContentItemMapper
) {
    fun getCourseContent(shouldSkipStoredValue: Boolean = false): Observable<Pair<Course, List<CourseContentItem>>> =
        courseObservableSource
            .skip(if (shouldSkipStoredValue) 1 else 0)
            .switchMap { course ->
                getEmptySections(course) concat getContent(course)
            }

    fun mustShowDeadlinesToolTip(): Single<Boolean> {
        return Single.fromCallable {
            val isTooltipShown = sharedPreferenceHelper.isPersonalDeadlinesTooltipShown
            sharedPreferenceHelper.afterPersonalDeadlinesTooltipShown()
            !isTooltipShown && personalDeadlinesSplitTest.currentGroup.isPersonalDeadlinesEnabled
        }
    }

    private fun getEmptySections(course: Course): Observable<Pair<Course, List<CourseContentItem>>> =
        Observable.just(course to emptyList())

    private fun getContent(course: Course): Observable<Pair<Course, List<CourseContentItem>>> =
        getSectionsOfCourse(course)
            .flatMap { populateSections(course, it) }
            .flatMapObservable { items ->
                Single
                    .concat(Single.just(course to items), loadUnits(course, items))
                    .toObservable()
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
                .getLessons(*units.mapToLongArray(Unit::lesson), primarySourceType = DataSourceType.REMOTE)
        )
            .map { (progresses, lessons) ->
                courseContentItemMapper.mapUnits(sectionItems, units, lessons, progresses)
            }
}