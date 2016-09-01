package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.CoursesStepicResponse
import org.stepic.droid.web.IApi
import retrofit.Response
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class PersistentCourseListPresenter(
        val analytic: Analytic,
        val databaseFacade: DatabaseFacade,
        val threadPoolExecutor: ThreadPoolExecutor,
        val mainHandler: IMainHandler,
        val api: IApi
) : PresenterBase<CoursesView>() {

    var currentPage = AtomicInteger(1);
    var hasNextPage = AtomicBoolean(true)
    var isLoading = AtomicBoolean(false)
    var isEmptyCourses = AtomicBoolean(false)

    fun restoreState() {
        if (isEmptyCourses.get() && !hasNextPage.get()) {
            view?.showEmptyCourses()
        }
    }

    /**
     * 1) Show from cache, if not empty (hide progress).
     * 2) Load from internet (if fail -> show)
     * 3) Save to db
     * 4) show from cache. (all states)
     */
    fun downloadData(courseType: DatabaseFacade.Table) {
        if (isLoading.get() || !hasNextPage.get()) return
        isLoading.set(true)

        threadPoolExecutor.execute {
            val coursesBeforeLoading = databaseFacade.getAllCourses(courseType).filterNotNull()
            if (coursesBeforeLoading.isNotEmpty() && currentPage.get() == 1) {
                mainHandler.post {
                    view?.showCourses(coursesBeforeLoading)
                }
            }
            else {
                mainHandler.post { view?.showLoading() }
            }

            val response: Response<CoursesStepicResponse>?
            try {
                if (courseType == DatabaseFacade.Table.featured) {
                    response = api.getFeaturedCourses(currentPage.get()).execute()
                } else {
                    response = api.getEnrolledCourses(currentPage.get()).execute()
                }
            } catch (ex: Exception) {
                response = null
            }

            if (response != null && response.isSuccess) {
                val coursesFromInternet = response.body().courses

                coursesFromInternet.filterNotNull().forEach {
                    databaseFacade.addCourse(it, courseType)
                }

                hasNextPage.set(response.body().meta.has_next)
                if (hasNextPage.get()) {
                    currentPage.set(response.body().meta.page + 1) // page for next loading
                }

                val allCourses = databaseFacade.getAllCourses(courseType)
                mainHandler.post {
                    if (allCourses.isEmpty()) {
                        isEmptyCourses.set(true)
                        view?.showEmptyCourses()
                    } else {
                        view?.showCourses(allCourses)
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

    fun refreshData(courseType: DatabaseFacade.Table) {
        analytic.reportEvent(Analytic.Interaction.PULL_TO_REFRESH_COURSE)
        if (isLoading.get()) return
        currentPage.set(1);
        hasNextPage.set(true)
        downloadData(courseType)
    }


}
