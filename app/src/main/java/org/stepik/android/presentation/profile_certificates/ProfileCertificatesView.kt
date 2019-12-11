package org.stepik.android.presentation.profile_certificates

import org.stepic.droid.model.CertificateViewItem

interface ProfileCertificatesView {
    sealed class State {
        object Idle : State()
        object SilentLoading : State()
        class Loading(val userId: Long) : State()
        class CertificatesLoaded(val certificates: List<CertificateViewItem>, val userId: Long) : State()
        object Error : State()
        object NoCertificates : State()
    }

    fun setState(state: State)
}