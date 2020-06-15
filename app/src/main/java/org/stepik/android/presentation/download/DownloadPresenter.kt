package org.stepik.android.presentation.download

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.downloads.interactor.DownloadInteractor
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.DownloadProgress
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepic.droid.util.size
import org.stepik.android.domain.download.interactor.DownloadsInteractor
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.download.mapper.DownloadItemsStateMapper
import javax.inject.Inject

class DownloadPresenter
@Inject
constructor(
    private val externalStorageManager: ExternalStorageManager,
    private val downloadsInteractor: DownloadsInteractor,
    private val downloadItemsStateMapper: DownloadItemsStateMapper,

    private val courseDownloadInteractor: DownloadInteractor<Course>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<DownloadView>() {
    private var state: DownloadView.State = DownloadView.State.Empty
        set(value) {
            field = value
            view?.setState(state)
        }

    private var isBlockingLoading = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    override fun attachView(view: DownloadView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchStorage() {
        val optionsObservable = Single.fromCallable(externalStorageManager::getAvailableStorageLocations)
            .cache()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)

        compositeDisposable += optionsObservable
            .map { storageLocations ->
                Triple(
                    storageLocations.map { location -> location.path.size() }.sum(),
                    storageLocations.map { location -> location.freeSpaceBytes }.sum(),
                    storageLocations.map { location -> location.totalSpaceBytes }.sum()
                )
            }
            .subscribeBy(
                onError = emptyOnErrorStub,
                onSuccess = { (contentSize, freeSpace, totalSpace) ->
                    view?.setStorageInfo(contentSize, freeSpace, totalSpace)
                }
            )
    }

    fun fetchDownloadedCourses() {
        compositeDisposable += downloadsInteractor.fetchDownloadItems()
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { state = downloadItemsStateMapper.replaceDownloadItem(state, it); fetchStorage() },
                onError = emptyOnErrorStub
            )
    }

    fun removeCourseDownload(course: Course) {
        isBlockingLoading = true
        compositeDisposable += courseDownloadInteractor
            .removeTask(course)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onComplete = { state = downloadItemsStateMapper.replaceDownloadItem(state, DownloadItem(course, DownloadProgress.Status.NotCached)); fetchStorage() },
                onError = { it.printStackTrace() }
            )
    }
}