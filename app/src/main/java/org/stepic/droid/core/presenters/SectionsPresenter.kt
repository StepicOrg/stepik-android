package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.SectionsView
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.IApi
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class SectionsPresenter(val threadPoolExecutor: ThreadPoolExecutor,
                        val mainHandler: IMainHandler,
                        val api: IApi,
                        val databaseFacade: DatabaseFacade) : PresenterBase<SectionsView>() {

    val sectionList: MutableList<Section> = ArrayList<Section>()
    val isLoading: AtomicBoolean = AtomicBoolean(false)

    fun showSections(course: Course?, isRefreshing: Boolean) {
        if (course == null) {
            view?.onEmptySections()
            return
        }

        if (sectionList.isNotEmpty() && !isRefreshing) {
            view?.onNeedShowSections(sectionList)
            return
        }
        if (isLoading.get()) return
        isLoading.set(true)
        view?.onLoading()
        threadPoolExecutor.execute {
            try {
                if (!isRefreshing) {
                    val sectionsFromCache = databaseFacade.getAllSectionsOfCourse(course).filterNotNull()
                    Collections.sort(sectionsFromCache, Comparator<org.stepic.droid.model.Section> { lhs, rhs ->
                        if (lhs == null || rhs == null) return@Comparator 0

                        val lhsPos = lhs.position
                        val rhsPos = rhs.position
                        lhsPos - rhsPos
                    })

                    if (sectionsFromCache.isNotEmpty()) {
                        mainHandler.post {
                            sectionList.clear()
                            sectionList.addAll(sectionsFromCache)
                            view?.onNeedShowSections(sectionList)
                        }
                    }
                }

                val sectionIds = course.sections
                if (sectionIds == null || sectionIds.isEmpty()) {
                    mainHandler.post {
                        view?.onEmptySections()
                    }
                } else {
                    //get from Internet
                    try {
                        val response = api.getSections(sectionIds).execute()
                        if (response.isSuccess || response.body()?.sections?.isNotEmpty() ?: false) {
                            val sections = response.body().sections
                            databaseFacade.removeSectionsOfCourse(course.courseId)
                            sections.forEach {
                                databaseFacade.addSection(it)
                            }

                            mainHandler.post {
                                sectionList.clear()
                                sectionList.addAll(sections)
                                if (sectionList.isEmpty()) {
                                    view?.onEmptySections()
                                } else {
                                    view?.onNeedShowSections(sectionList)
                                }
                            }
                            sections.forEach {
                                databaseFacade.addSection(it)
                            }
                        } else {
                            mainHandler.post {
                                view?.onConnectionProblem()
                            }
                        }
                    } catch (exception: Exception) {
                        mainHandler.post {
                            view?.onConnectionProblem()
                        }
                    }
                }
            } finally {
                isLoading.set(false)
            }
        }
    }

}