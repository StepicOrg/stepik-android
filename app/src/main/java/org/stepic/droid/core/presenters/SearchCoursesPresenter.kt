package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.model.Course
import org.stepic.droid.util.resolvers.SearchResolver
import org.stepic.droid.web.IApi
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SearchCoursesPresenter(val api: IApi,
                             val threadPoolExecutor: ThreadPoolExecutor,
                             val mainHandler: MainHandler,
                             val searchResolver: SearchResolver) : PresenterBase<CoursesView>() {

    private var isLoading = AtomicBoolean(false)
    private var currentPage = AtomicInteger(1)
    private var hasNextPage = AtomicBoolean(true)
    var isEmptyCourses = AtomicBoolean(false)

    fun restoreState() {
        if (isEmptyCourses.get() && !hasNextPage.get()) {
            view?.showEmptyCourses()
        }
    }

    fun downloadData(searchQuery: String) {
        if (isLoading.get() || !hasNextPage.get()) return

        if (hasNextPage.get() && isLoading.compareAndSet(false, true)) {
            view?.showLoading()
            threadPoolExecutor.execute {
                try {
                    api.getSearchResultsCourses(currentPage.get(), searchQuery).execute()

                    val searchResultResponseBody = api.getSearchResultsCourses(currentPage.get(), searchQuery).execute().body()
                    val searchResultList = searchResultResponseBody.searchResultList
                    val courseIdsForSearch = searchResolver.getCourseIdsFromSearchResults(searchResultList)
                    hasNextPage.set(searchResultResponseBody.meta.has_next)
                    currentPage.set(searchResultResponseBody.meta.page + 1)

                    if (courseIdsForSearch.isEmpty()) {
                        mainHandler.post {
                            view?.showEmptyCourses()
                        }
                    } else {
                        val courses = api.getCourses(1, courseIdsForSearch).execute().body().courses //FIXME: WARNING, here may pagination not working for query with ids[]
                        if (courses == null || courses.isEmpty()) {
                            mainHandler.post { view?.showEmptyCourses() }
                        } else {
                            val sortedCopy = ArrayList<Course>(courses.size)
                            var forInsert: Course? = null
                            for (searchId in courseIdsForSearch) {
                                for (cachedCourse in courses) {
                                    if (cachedCourse.courseId == searchId) {
                                        forInsert = cachedCourse
                                        break
                                    }
                                }
                                if (forInsert != null) {
                                    sortedCopy.add(forInsert)
                                }
                                forInsert = null
                            }
                            mainHandler.post {
                                view?.showCourses(sortedCopy.filterNotNull())
                            }

                        }

                    }


                } catch (exception: Exception) {
                    mainHandler.post {
                        view?.showConnectionProblem()
                    }
                } finally {
                    isLoading.set(false)
                }

            }
        }
    }

    fun refreshData(searchQuery: String) {
        if (isLoading.get()) return
        currentPage.set(1);
        hasNextPage.set(true)
        downloadData(searchQuery)
    }
}
