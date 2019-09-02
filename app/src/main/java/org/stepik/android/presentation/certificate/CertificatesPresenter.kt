package org.stepik.android.presentation.certificate

import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.concatWithPagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.certificate.interactor.CertificatesInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CertificatesPresenter
@Inject
constructor(
    private val certificatesInteractor: CertificatesInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CertificatesView>() {
    private var state: CertificatesView.State = CertificatesView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable
    }

    override fun attachView(view: CertificatesView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCertificates(userId: Long) {
        if (state != CertificatesView.State.Idle) return

        state = CertificatesView.State.Loading
        paginationDisposable += fetchCertificatesFromCache(userId)
            .switchIfEmpty(fetchCertificatesFromRemote(userId))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = it
                    if (state is CertificatesView.State.CertificatesCache) {
                        fetchNextPageFromRemote(userId)
                    }
                },
                onError   = { state = CertificatesView.State.NetworkError }
            )
    }

    fun fetchNextPageFromRemote(userId: Long) {
        val oldState = state

        val oldItems = (oldState as? CertificatesView.State.CertificatesRemote)?.certificates
            ?: (oldState as? CertificatesView.State.CertificatesCache)?.certificates
            ?: return

        val currentItems =
            when {
                oldState is CertificatesView.State.CertificatesRemote
                        && oldState.certificates.hasNext ->
                    oldState.certificates

                oldState is CertificatesView.State.CertificatesCache ->
                    emptyList<CertificateViewItem>()

                else -> return
            }

        val nextPage = (currentItems as? PagedList<CertificateViewItem>)
            ?.page
            ?.plus(1)
            ?: 1

        state = CertificatesView.State.CertificatesRemoteLoading(oldItems)
        paginationDisposable += certificatesInteractor
            .getCertificates(userId, page = nextPage, sourceType = DataSourceType.REMOTE)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = CertificatesView.State.CertificatesRemote(currentItems.concatWithPagedList(it))
                    if (oldState is CertificatesView.State.CertificatesCache) {
                        fetchNextPageFromRemote(userId) // load 2 page from remote after going online
                    }
                },
                onError = { state = oldState; view?.showNetworkError() }
            )
    }

    fun forceUpdate(userId: Long) {
        paginationDisposable.clear()

        val oldState = state

        state = CertificatesView.State.Loading
        paginationDisposable += certificatesInteractor
            .getCertificates(userId, page = 1, sourceType = DataSourceType.REMOTE)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { certificates ->
                    state =
                        if (certificates.isEmpty()) {
                            CertificatesView.State.EmptyCertificates
                        } else {
                            CertificatesView.State.CertificatesRemote(certificates)
                        }
                },
                onError = {
                    when (oldState) {
                        is CertificatesView.State.CertificatesCache,
                        is CertificatesView.State.CertificatesRemote -> {
                            state = oldState
                            view?.showNetworkError()
                        }
                        else ->
                            state = CertificatesView.State.NetworkError
                    }
                }
            )
    }

    private fun fetchCertificatesFromCache(userId: Long): Maybe<CertificatesView.State> =
        certificatesInteractor
            .getCertificates(userId, page = 1, sourceType = DataSourceType.CACHE)
            .filter { it.isNotEmpty() }
            .map { CertificatesView.State.CertificatesCache(it) }

    private fun fetchCertificatesFromRemote(userId: Long): Single<CertificatesView.State> =
        certificatesInteractor
            .getCertificates(userId, page = 1, sourceType = DataSourceType.REMOTE)
            .map { certificates ->
                if (certificates.isEmpty()) {
                    CertificatesView.State.EmptyCertificates
                } else {
                    CertificatesView.State.CertificatesRemote(certificates)
                }
            }
}