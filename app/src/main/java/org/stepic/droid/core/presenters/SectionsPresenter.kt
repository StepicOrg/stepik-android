package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.SectionsView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.downloads.interactor.DownloadInteractor
import org.stepic.droid.persistence.downloads.progress.DownloadProgressProvider
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.preferences.UserPreferences
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.transformers.transformToViewModel
import org.stepic.droid.util.addDisposable
import org.stepic.droid.viewmodel.ProgressViewModel
import org.stepic.droid.web.Api
import timber.log.Timber
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@CourseAndSectionsScope
class SectionsPresenter
@Inject
constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val api: Api,
        private val databaseFacade: DatabaseFacade,

        private val sectionDownloadInteractor: DownloadInteractor<Section>,
        private val sectionDownloadProgressProvider: DownloadProgressProvider<Section>,
        private val userPreferences: UserPreferences,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) : PresenterBase<SectionsView>() {

    private val sectionList: MutableList<Section> = ArrayList()
    private val isLoading: AtomicBoolean = AtomicBoolean(false)
    private var cachedCourseId = 0L
    val progressMap: HashMap<String, ProgressViewModel> = HashMap()

    private val compositeDisposable = CompositeDisposable()
    private var progressDisposable: Disposable? = null

    private val pendingSections = mutableSetOf<Long>()

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
                            showSections(sectionList)
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
                                databaseFacade.addSection(it)
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
                                    showSections(sectionList)
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
                        progressMap[progressId] = progressViewModel
                        view?.updatePosition(position)
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    @WorkerThread
    private fun fetchProgresses(sectionList: List<Section>): Map<String, ProgressViewModel> = try {
        val progressIds = sectionList.mapNotNull { it.progress }.toTypedArray()
        val progresses = api.getProgresses(progressIds).execute().body()!!.progresses
        val progressIdToProgressViewModel = progresses.mapNotNull { it.transformToViewModel() }.associateBy { it.progressId }
        threadPoolExecutor.execute {
            //save to database
            progresses.filterNotNull().forEach { databaseFacade.addProgress(it) }
        }
        progressIdToProgressViewModel
    } catch (exception: Exception) {
        Timber.d("cant show progresses")
        emptyMap()
    }

    private fun showSections(sectionList: MutableList<Section>) {
        view?.onNeedShowSections(sectionList)
        progressDisposable?.dispose()
        progressDisposable = sectionDownloadProgressProvider
                .getProgress(*sectionList.toTypedArray())
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy({ it.printStackTrace() }) {
                    if (it.id !in pendingSections || it.status !is DownloadProgress.Status.InProgress) {
                        view?.showDownloadProgress(it)
                    }
                }
    }

    fun addDownloadTask(section: Section) {
        if (section.id in pendingSections) return

        val allowedNetworkTypes = if (userPreferences.isNetworkMobileAllowed) {
            EnumSet.of(DownloadConfiguration.NetworkType.WIFI, DownloadConfiguration.NetworkType.MOBILE)
        } else {
            EnumSet.of(DownloadConfiguration.NetworkType.WIFI)
        }

        pendingSections.add(section.id)
        view?.showDownloadProgress(DownloadProgress(section.id, DownloadProgress.Status.Pending))

        compositeDisposable addDisposable sectionDownloadInteractor
                .addTask(section, configuration = DownloadConfiguration(
                        allowedNetworkTypes,
                        userPreferences.qualityVideo
                ))
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy({
                    pendingSections.remove(section.id)
                }) {
                    pendingSections.remove(section.id)
                }
    }

    fun removeDownloadTask(section: Section) {
        if (section.id in pendingSections) return

        pendingSections.add(section.id)
        view?.showDownloadProgress(DownloadProgress(section.id, DownloadProgress.Status.Pending))
        compositeDisposable addDisposable sectionDownloadInteractor
                .removeTask(section)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy({
                    it.printStackTrace()
                    pendingSections.remove(section.id)
                }) {
                    pendingSections.remove(section.id)
                }
    }

    override fun detachView(view: SectionsView) {
        progressDisposable?.dispose()
        super.detachView(view)
    }
}