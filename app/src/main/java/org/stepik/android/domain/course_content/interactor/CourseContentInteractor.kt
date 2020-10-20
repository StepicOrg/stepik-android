package org.stepik.android.domain.course_content.interactor

import com.google.firebase.perf.FirebasePerformance
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import ru.nobird.android.core.model.mapToLongArray
import org.stepic.droid.util.plus
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.presentation.course_content.mapper.CourseContentItemMapper
import org.stepik.android.view.course_content.model.CourseContentItem
import java.util.concurrent.atomic.AtomicInteger
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
    companion object {
        private const val UNITS_CHUNK_SIZE = 10
    }

    fun getCourseContent(shouldSkipStoredValue: Boolean = false): Observable<Pair<Course, List<CourseContentItem>>> {
        val count = AtomicInteger(0)

        return courseObservableSource
            .skip(if (shouldSkipStoredValue) 1 else 0)
            .switchMap { course ->
                val shouldUseCache = !shouldSkipStoredValue && count.getAndIncrement() < 1
                val contentObservable =
                    if (shouldUseCache) {
                        val cacheSource = getContent(course, emptyList(), DataSourceType.CACHE)
                            .share()

                        val remoteSource = cacheSource
                            .lastOrError()
                            .flatMapObservable { (_, items) ->
                                getContent(course, items, DataSourceType.REMOTE)
                            }

                        Observable.concat(cacheSource, remoteSource)
                    } else {
                        getContent(course, emptyList(), DataSourceType.REMOTE)
                    }

                Observable.concat(getEmptySections(course), contentObservable)
            }
    }

    private fun getEmptySections(course: Course): Observable<Pair<Course, List<CourseContentItem>>> =
        Observable.just(course to emptyList())

    private fun getContent(course: Course, items: List<CourseContentItem>, dataSourceType: DataSourceType): Observable<Pair<Course, List<CourseContentItem>>> {
        val courseContentLoadingTrace = FirebasePerformance.getInstance().newTrace(Analytic.Traces.COURSE_CONTENT_LOADING)
        courseContentLoadingTrace.putAttribute(AmplitudeAnalytic.Course.Params.COURSE, course.id.toString())
        courseContentLoadingTrace.putAttribute(AmplitudeAnalytic.Course.Params.SOURCE, dataSourceType.name)
        courseContentLoadingTrace.start()

        return getSectionsOfCourse(course, dataSourceType)
            .flatMap { populateSections(course, it, items, dataSourceType) }
            .flatMapObservable { populatedItems ->
                Observable.just(course to populatedItems) + loadUnits(course, populatedItems, dataSourceType)
            }
            .doOnComplete {
                courseContentLoadingTrace.stop()
            }
    }

    private fun getSectionsOfCourse(course: Course, dataSourceType: DataSourceType): Single<List<Section>> =
        sectionRepository
            .getSections(course.sections ?: listOf(), primarySourceType = dataSourceType)

    private fun populateSections(course: Course, sections: List<Section>, items: List<CourseContentItem>, dataSourceType: DataSourceType): Single<List<CourseContentItem>> =
        if (dataSourceType == DataSourceType.CACHE) {
            Single.just(emptyList())
        } else {
            progressRepository
                .getProgresses(sections.getProgresses())
        }
            .map { progresses ->
                courseContentItemMapper.mapSectionsWithEmptyUnits(course, sections, items.filterIsInstance<CourseContentItem.UnitItem>(), progresses)
            }

    private fun loadUnits(course: Course, items: List<CourseContentItem>, dataSourceType: DataSourceType): Observable<Pair<Course, List<CourseContentItem>>> =
        Observable
            .fromCallable { courseContentItemMapper.getUnitPlaceholdersIds(items) }
            .flatMap { unitIds ->
                val subject = PublishSubject.create<List<String>>()

                val unitsSource = unitIds
                    .chunked(UNITS_CHUNK_SIZE)
                    .toObservable()
                    .concatMapSingle { ids ->
                        getUnits(ids, dataSourceType)
                            .flatMap { units ->
                                lessonRepository
                                    .getLessons(*units.mapToLongArray(Unit::lesson), primarySourceType = dataSourceType)
                                    .map { units to it }
                                    .doOnSuccess { subject.onNext(units.getProgresses()) }
                            }
                    }

                val progressesSource =
                    if (dataSourceType == DataSourceType.CACHE) {
                        Observable.just(emptyList())
                    } else {
                        Observable.concat(
                            Observable.just(emptyList()),
                            subject
                                .observeOn(Schedulers.io())
                                .flatMapSingle { progressIds ->
                                    progressRepository.getProgresses(progressIds)
                                }
                        )
                    }

                Observables
                    .combineLatest(progressesSource, unitsSource) { progresses, (units, lessons) ->
                        Triple(units, lessons, progresses)
                    }
                    .scan(items) { newItems, (units, lessons, progresses) ->
                        val sectionItems = newItems
                            .filterIsInstance<CourseContentItem.SectionItem>()
                        val unitItems = courseContentItemMapper.mapUnits(course, sectionItems, units, lessons, progresses)

                        courseContentItemMapper.replaceUnits(newItems, unitItems, progresses)
                    }
            }
            .map { course to it }

    private fun getUnits(unitIds: List<Long>, dataSourceType: DataSourceType): Single<List<Unit>> =
        unitRepository
            .getUnits(unitIds, primarySourceType = dataSourceType)
}