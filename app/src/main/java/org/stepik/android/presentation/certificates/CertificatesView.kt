package org.stepik.android.presentation.certificates

import org.stepic.droid.model.CertificateViewItem

interface CertificatesView {
    sealed class State {
        object Idle : State()
        object Error : State()
        object Loading : State()
        class CertificatesLoaded(val certificates: List<CertificateViewItem>)
    }

    fun setState(state: State)
}