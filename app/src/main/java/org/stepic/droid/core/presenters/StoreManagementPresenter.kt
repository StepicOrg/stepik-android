package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.core.presenters.contracts.StoreManagementView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.downloads.interactor.RemovalDownloadsInteractor
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.persistence.service.FileTransferService
import org.stepic.droid.util.size
import javax.inject.Inject

class StoreManagementPresenter
@Inject
constructor(
    private val externalStorageManager: ExternalStorageManager,
    private val removalDownloadsInteractor: RemovalDownloadsInteractor,
    private val fileTransferEventSubject: PublishSubject<FileTransferService.Event>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StoreManagementView>() {
    private val compositeDisposable = CompositeDisposable()

    override fun attachView(view: StoreManagementView) {
        super.attachView(view)
        fetchStorage()
    }

    private fun fetchStorage() {
        val optionsObservable = Single
            .fromCallable(externalStorageManager::getAvailableStorageLocations)
            .cache()

        val currentStorageSource = Single
            .fromCallable(externalStorageManager::getSelectedStorageLocation)

        compositeDisposable += Singles
            .zip(optionsObservable, currentStorageSource)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { (locations, selectedLocation) ->
                    view?.setStorageOptions(locations, selectedLocation)
                },
                onError = { view?.setStorageOptions(emptyList(), null) }
            )

        compositeDisposable += optionsObservable
            .map { it.map { location -> location.path.size() }.sum() }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { view?.setUpClearCacheButton(it) },
                onError = { view?.setUpClearCacheButton(0) }
            )
    }

    fun removeAllDownloads() {
        view?.showLoading()
        removalDownloadsInteractor
            .removeAllDownloads()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally { fetchStorage() }
            .subscribeBy(
                onComplete = { view?.hideLoading() },
                onError = { view?.hideLoading() }
            )
    }

    fun changeStorageLocation(storage: StorageLocation) {
        view?.showLoading(true)
        fileTransferEventSubject
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribe { view?.hideLoading() }
        externalStorageManager.setStorageLocation(storage)
    }

    override fun detachView(view: StoreManagementView) {
        compositeDisposable.clear()
        super.detachView(view)
    }
}