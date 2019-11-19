package org.stepic.droid.core.presenters

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.di.tags.TagScope
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.resolvers.SearchResolver
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.tags.repository.TagsRepository
import org.stepik.android.model.Course
import org.stepik.android.model.SearchResult
import org.stepik.android.model.Tag
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@TagScope
class TagListPresenter
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val tag: Tag,
    private val courseRepository: CourseRepository,
    private val tagsRepository: TagsRepository,
    private val searchResolver: SearchResolver,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler
) : PresenterBase<CoursesView>() {

    private val currentPage = AtomicInteger(1)
    private val hasNextPage = AtomicBoolean(true)
    private val compositeDisposable = CompositeDisposable()
    private val publisher = PublishSubject.create<Int>()

    fun onInitTag() {
        compositeDisposable += publisher
            .observeOn(mainScheduler)
            .doOnNext {
                view?.showLoading()
            }
            .observeOn(backgroundScheduler)
            .flatMap {
                tagsRepository.getSearchResultsOfTag(it, tag, getLang())
                    .toObservable()
            }
            .doOnNext { handleMeta(it) }
            .map { searchResolver.getCourseIdsFromSearchResults(it) }
            .flatMap {
                zipIdsAndCourses(it)
            }
            .map {
                sortByIdsInSearch(it.first, it.second)
            }
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { courses ->
                    if (courses.isEmpty()) {
                        view?.showEmptyCourses()
                    } else {
                        view?.showCourses(courses)
                    }
                },
                onError = {
                    onInitTag()
                    view?.showConnectionProblem()
                }
            )
    }


    fun downloadData() {
        if (hasNextPage.get()) {
            publisher.onNext(currentPage.get())
        }
    }

    private fun handleMeta(meta: PagedList<SearchResult>) {
        hasNextPage.set(meta.hasNext)
        currentPage.set(meta.page + 1)
    }

    private fun zipIdsAndCourses(it: LongArray): Observable<Pair<LongArray, List<Course>>>? {
        return Observable.zip(
                Observable.just(it),
                courseRepository
                    .getCourses(*it, primarySourceType = DataSourceType.REMOTE)
                    .toObservable(),
                BiFunction<LongArray, List<Course>, Pair<LongArray, List<Course>>> { courseIds, courses ->
                    courseIds to courses
                }
        )
    }

    private fun sortByIdsInSearch(courseIds: LongArray, courses: List<Course>): List<Course> {
        val idToPositionMap: Map<Long, Int> = courseIds
                .withIndex()
                .associate { Pair(it.value, it.index) }

        return courses.sortedWith(Comparator { firstCourse, secondCourse ->
            val firstPosition = idToPositionMap[firstCourse.id] ?: return@Comparator 0
            val secondPosition = idToPositionMap[secondCourse.id] ?: return@Comparator 0

            return@Comparator (firstPosition - secondPosition)
        })
    }

    fun refreshData() {
        reset()
        downloadData()
    }

    private fun reset() {
        currentPage.set(1)
        hasNextPage.set(true)
    }

    override fun detachView(view: CoursesView) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    private fun getLang(): String {
        val enumSet = sharedPreferenceHelper.filterForFeatured
        return enumSet.iterator().next().language
    }
}
