package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.SectionsView
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.transformers.transformToViewModel
import org.stepic.droid.web.IApi
import timber.log.Timber
import viewmodel.ProgressViewModel
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class SectionsPresenter(val threadPoolExecutor: ThreadPoolExecutor,
                        val mainHandler: IMainHandler,
                        val api: IApi,
                        val databaseFacade: DatabaseFacade) : PresenterBase<SectionsView>() {

    val sectionList: MutableList<Section> = ArrayList<Section>()
    val isLoading: AtomicBoolean = AtomicBoolean(false)
    val progressMap: HashMap<String, ProgressViewModel> = HashMap()

    fun showSections(course: Course?, isRefreshing: Boolean) {
        if (course == null) {
            view?.onEmptySections()
            return
        }

        if (sectionList.isNotEmpty() && !isRefreshing) {
            view?.onNeedShowSections(sectionList, progressMap)
            return
        }
        if (!isLoading.compareAndSet(/* expect */ false, true)) return //if false -> set true and return true, if true -> return false
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
                        val progressMapLocal = HashMap<String, ProgressViewModel>()
                        sectionsFromCache.forEach {
                            val progressId = it.progress
                            if (progressId != null) {
                                val progressViewModel = databaseFacade.getProgressById(progressId)?.transformToViewModel()
                                progressViewModel?.let {
                                    progressMapLocal.put(progressId, progressViewModel)
                                }
                            }
                        }
                        mainHandler.post {
                            progressMap.clear()
                            progressMap.putAll(progressMapLocal)
                            sectionList.clear()
                            sectionList.addAll(sectionsFromCache)
                            view?.onNeedShowSections(sectionList, progressMap)
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
                            val cachedSections: Map<Long, Section> = databaseFacade
                                    .getAllSectionsOfCourse(course)
                                    .filterNotNull()
                                    .associateBy { it.id }
                            databaseFacade.removeSectionsOfCourse(course.courseId)
                            sections.forEach {
                                val cachedSection: Section? = cachedSections[it.id]
                                if (cachedSection != null) {
                                    it.is_cached = cachedSection.is_cached
                                    it.is_loading = cachedSection.is_loading
                                }
                                databaseFacade.addSection(it)
                                databaseFacade.updateOnlyCachedLoadingSection(it)
                            }

                            val progressMapOnBackground = fetchProgresses(sections)// we already shown cached sections, now show from Internet

                            mainHandler.post {
                                progressMap.clear()
                                progressMap.putAll(progressMapOnBackground)
                                sectionList.clear()
                                sectionList.addAll(sections)
                                if (sectionList.isEmpty()) {
                                    view?.onEmptySections()
                                } else {
                                    view?.onNeedShowSections(sectionList, progressMap)
                                }
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

    @WorkerThread
    private fun fetchProgresses(sectionList: List<Section>): Map<String, ProgressViewModel> {
        try {
            val progressIds
                    = sectionList
                    .map { it.progress }
                    .filterNotNull()
                    .toTypedArray()
            val progresses = api.getProgresses(progressIds).execute().body().progresses
            val progressIdToProgressViewModel = progresses.map { it.transformToViewModel() }.filterNotNull().associateBy { it.progressId }
            threadPoolExecutor.execute {
                //save to database
                progresses.filterNotNull().forEach { databaseFacade.addProgress(it) }
            }
            return progressIdToProgressViewModel
        } catch (exception: Exception) {
            Timber.d("cant show progresses")
            return emptyMap()
        }

    }

}