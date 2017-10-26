package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.model.Course
import org.stepic.droid.util.resolvers.SearchResolver
import org.stepic.droid.web.Api
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@CourseListScope
class SearchCoursesPresenter
@Inject constructor(
        private val api: Api,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val searchResolver: SearchResolver,
        private val analytic: Analytic)
    : PresenterBase<CoursesView>() {

    private var isLoading = AtomicBoolean(false)
    private var currentPage = AtomicInteger(1)
    private var hasNextPage = AtomicBoolean(true)
    private var isEmptyCourses = AtomicBoolean(false)

    fun restoreState() {
        if (isEmptyCourses.get() && !hasNextPage.get()) {
            view?.showEmptyCourses()
        }
    }

    fun downloadData(searchQuery: String?) {
        if (isLoading.get() || !hasNextPage.get()) return

        if (hasNextPage.get() && isLoading.compareAndSet(false, true)) {
            if (searchQuery == null) {
                analytic.reportEvent(Analytic.Search.SEARCH_NULL)
            } else {
                analytic.reportEventWithName(Analytic.Search.SEARCH_QUERY, searchQuery)
            }
            view?.showLoading()
            threadPoolExecutor.execute {
                try {
                    val response = api.getSearchResultsCourses(currentPage.get(), searchQuery).execute()
                    if (!response.isSuccessful) {
                        analytic.reportEvent(Analytic.Error.SEARCH_COURSE_UNSUCCESSFUL, "${response.code()}  ${response.errorBody()?.string()}")
                    }

                    val searchResultResponseBody = response.body()!!
                    val searchResultList = searchResultResponseBody.searchResultList
                    val courseIdsForSearch = searchResolver.getCourseIdsFromSearchResults(searchResultList)
                    hasNextPage.set(searchResultResponseBody.meta.has_next)
                    currentPage.set(searchResultResponseBody.meta.page + 1)

                    if (courseIdsForSearch.isEmpty()) {
                        mainHandler.post {
                            view?.showEmptyCourses()
                        }
                    } else {
                        val courses = api.getCourses(1, courseIdsForSearch).execute().body()?.courses //FIXME: WARNING, here may pagination not working for query with ids[]
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
                                view?.showCourses(sortedCopy)
                            }

                        }

                    }


                } catch (exception: Exception) {
                    analytic.reportError(Analytic.Error.SEARCH_COURSE_NO_INTERNET, exception)
                    mainHandler.post {
                        view?.showConnectionProblem()
                    }
                } finally {
                    isLoading.set(false)
                }

            }
        }
    }

    fun refreshData(searchQuery: String?) {
        if (isLoading.get()) return
        currentPage.set(1);
        hasNextPage.set(true)
        downloadData(searchQuery)
    }
}
