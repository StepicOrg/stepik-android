package org.stepik.android.presentation.certificates

import org.stepic.droid.model.CertificateViewItem

interface CertificatesView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object EmptyCertificates : State()
        object NetworkError : State()
        class CertificatesLoaded(val certificates: List<CertificateViewItem>) : State()
    }

    fun setState(state: State)
}