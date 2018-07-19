package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.SectionsView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.transformers.transformToViewModel
import org.stepic.droid.viewmodel.ProgressViewModel
import org.stepic.droid.web.Api
import timber.log.Timber
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@CourseAndSectionsScope
class SectionsPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val api: Api,
        private val databaseFacade: DatabaseFacade) : PresenterBase<SectionsView>() {

    private val sectionList: MutableList<Section> = ArrayList()
    private val isLoading: AtomicBoolean = AtomicBoolean(false)
    private var cachedCourseId = 0L
    val progressMap: HashMap<String, ProgressViewModel> = HashMap()

    fun showSections(course: Course?, isRefreshing: Boolean) {
        if (course == null) {
            view?.onEmptySections()
            return
        }

        if (sectionList.isNotEmpty() && !isRefreshing && cachedCourseId == course.id) {
            view?.onNeedShowSections(sectionList)
            return
        }
        if (!isLoading.compareAndSet(/* expect */ false, true)) return //if false -> set true and return true, if true -> return false
        view?.onLoading()
        threadPoolExecutor.execute {
            try {
                if (!isRefreshing) {
                    val sectionsFromCache = databaseFacade.getAllSectionsOfCourse(course).filterNotNull()
                    Collections.sort(sectionsFromCache, Comparator { lhs, rhs ->
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
                            cachedCourseId = course.id
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
                        val sections = api.getSections(sectionIds).execute()?.body()?.sections
                        if (sections?.isNotEmpty() == true) {
                            val cachedSections: Map<Long, Section> = databaseFacade
                                    .getAllSectionsOfCourse(course)
                                    .filterNotNull()
                                    .associateBy { it.id }
                            databaseFacade.removeSectionsOfCourse(course.id)
                            sections.forEach {
                                val cachedSection: Section? = cachedSections[it.id]
                                if (cachedSection != null) {
                                    it.isCached = cachedSection.isCached
                                    it.isLoading = cachedSection.isLoading
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
                                cachedCourseId = course.id
                                if (sectionList.isEmpty()) {
                                    view?.onEmptySections()
                                } else {
                                    view?.onNeedShowSections(sectionList)
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

    @MainThread
    fun updateSectionProgress(progress: Progress) {
        threadPoolExecutor.execute {
            try {
                //this progress should be already in database, just prepare for showing
                val progressViewModel = progress.transformToViewModel()
                var position: Int = -1
                sectionList.forEachIndexed { index, section ->
                    if (section.progress == progress.id) {
                        position = index
                        return@forEachIndexed
                    }
                }
                val progressId = progress.id
                if (position >= 0 && progressViewModel != null && progressId != null) {
                    mainHandler.post {
                        progressMap.put(progressId, progressViewModel)
                        view?.updatePosition(position)
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
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
            val progresses = api.getProgresses(progressIds).execute().body()!!.progresses
            val progressIdToProgressViewModel = progresses.mapNotNull { it.transformToViewModel() }.associateBy { it.progressId }
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