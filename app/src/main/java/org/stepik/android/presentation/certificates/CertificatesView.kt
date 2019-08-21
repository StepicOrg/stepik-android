package org.stepik.android.presentation.certificates

import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.util.PagedList

interface CertificatesView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object EmptyCertificates : State()
        object NetworkError : State()

        class CertificatesCache(val certificates: PagedList<CertificateViewItem>) : State()
        class CertificatesRemote(val certificates: PagedList<CertificateViewItem>) : State()
        class CertificatesRemoteLoading(val certificates: PagedList<CertificateViewItem>) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}