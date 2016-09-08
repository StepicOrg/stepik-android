package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.model.Course
import org.stepic.droid.util.resolvers.ISearchResolver
import org.stepic.droid.web.CoursesStepicResponse
import org.stepic.droid.web.IApi
import org.stepic.droid.web.SearchResultResponse
import retrofit.Response
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SearchCoursesPresenter(val api: IApi,
                             val threadPoolExecutor: ThreadPoolExecutor,
                             val mainHandler: IMainHandler,
                             val searchResolver: ISearchResolver) : PresenterBase<CoursesView>() {

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
        isLoading.set(true)
        view?.showLoading()
        threadPoolExecutor.execute {
            val searchResponse: Response<SearchResultResponse>?
            try {
                searchResponse = api.getSearchResultsCourses(currentPage.get(), searchQuery).execute()
            } catch (ex: Exception) {
                searchResponse = null
            }

            if (searchResponse != null && searchResponse.isSuccess) {
                val searchResultList = searchResponse.body().searchResultList
                val courseIdsForSearch = searchResolver.getCourseIdsFromSearchResults(searchResultList)
                hasNextPage.set(searchResponse.body().meta.has_next)
                currentPage.set(searchResponse.body().meta.page + 1)

                if (courseIdsForSearch.size == 0) {
                    mainHandler.post {
                        view?.showEmptyCourses()
                    }
                } else {
                    val courseResponse: Response<CoursesStepicResponse>?
                    try {
                        courseResponse = api.getCourses(1, courseIdsForSearch).execute()
                    } catch (ex: Exception) {
                        courseResponse = null
                    }

                    if (courseResponse != null && courseResponse.isSuccess) {
                        val courses = courseResponse.body().courses
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

                    } else {
                        mainHandler.post {
                            view?.showConnectionProblem()
                        }
                    }

                }


            } else {
                mainHandler.post {
                    view?.showConnectionProblem()
                }
            }

            isLoading.set(false)
        }
    }

    fun refreshData(searchQuery: String) {
        if (isLoading.get()) return
        currentPage.set(1);
        hasNextPage.set(true)
        downloadData(searchQuery)
    }
}
