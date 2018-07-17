package org.stepic.droid.core.presenters

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.di.tags.TagScope
import org.stepik.android.model.structure.Course
import org.stepic.droid.util.resolvers.SearchResolver
import org.stepic.droid.web.Api
import org.stepik.android.model.Meta
import org.stepik.android.model.learning.Tag
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@TagScope
class TagListPresenter
@Inject
constructor(
        private val tag: Tag,
        private val api: Api,
        private val searchResolver: SearchResolver,
        @MainScheduler
        private val mainScheduler: Scheduler,
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler
) : PresenterBase<CoursesView>() {
    companion object {
        private const val FIRST_PAGE = 1
    }


    private val currentPage = AtomicInteger(1)
    private val hasNextPage = AtomicBoolean(true)
    private val compositeDisposable = CompositeDisposable()
    private val publisher = PublishSubject.create<Int>()

    fun onInitTag() {
        val disposable = publisher
                .observeOn(mainScheduler)
                .doOnNext {
                    view?.showLoading()
                }
                .observeOn(backgroundScheduler)
                .flatMap {
                    api.getSearchResultsOfTag(it, tag)
                            .toObservable()
                }
                .doOnNext { handleMeta(it.meta) }
                .map { it.searchResultList }
                .map { searchResolver.getCourseIdsFromSearchResults(it) }
                .flatMap {
                    zipIdsAndCourses(it)
                }
                .map {
                    sortByIdsInSearch(it.first, it.second)
                }
                .observeOn(mainScheduler)
                .subscribe(
                        {
                            if (it.isEmpty()) {
                                view?.showEmptyCourses()
                            } else {
                                view?.showCourses(it)
                            }
                        },
                        { _ ->
                            onInitTag()
                            view?.showConnectionProblem()
                        }
                )
        compositeDisposable.add(disposable)
    }


    fun downloadData() {
        if (hasNextPage.get()) {
            publisher.onNext(currentPage.get())
        }
    }

    private fun handleMeta(meta: Meta) {
        hasNextPage.set(meta.hasNext)
        currentPage.set(meta.page + 1)
    }

    private fun zipIdsAndCourses(it: LongArray): Observable<Pair<LongArray, List<Course>>>? {
        return Observable.zip(
                Observable.just(it),
                api
                        .getCoursesReactive(FIRST_PAGE, it)
                        .toObservable()
                        .map { it.courses },
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
}
