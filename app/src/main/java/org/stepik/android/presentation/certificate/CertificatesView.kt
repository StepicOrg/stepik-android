package org.stepik.android.presentation.certificate

import org.stepic.droid.model.CertificateListItem
import org.stepik.android.model.Certificate
import ru.nobird.app.core.model.PagedList

interface CertificatesView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object EmptyCertificates : State()
        object NetworkError : State()

        class CertificatesCache(val certificates: PagedList<CertificateListItem.Data>) : State()
        class CertificatesRemote(val certificates: PagedList<CertificateListItem.Data>) : State()
        class CertificatesRemoteLoading(val certificates: PagedList<CertificateListItem.Data>) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
    fun setBlockingLoading(isLoading: Boolean)

    fun showChangeNameSuccess()
    fun showChangeNameDialogError(certificate: Certificate, attemptedFullName: String)
}