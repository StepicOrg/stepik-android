package org.stepik.android.presentation.profile_certificates

import org.stepic.droid.model.CertificateListItem
import org.stepik.android.domain.profile.model.ProfileData

interface ProfileCertificatesView {
    sealed class State {
        object Idle : State()
        object SilentLoading : State()
        class Loading(val profileData: ProfileData) : State()
        class CertificatesCache(val certificates: List<CertificateListItem.Data>, val profileData: ProfileData) : State()
        class CertificatesRemote(val certificates: List<CertificateListItem.Data>, val profileData: ProfileData) : State()
        object Error : State()
        object NoCertificates : State()
    }

    fun setState(state: State)
}