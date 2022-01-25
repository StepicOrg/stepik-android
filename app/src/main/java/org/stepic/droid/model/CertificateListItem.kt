package org.stepic.droid.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.Certificate

sealed class CertificateListItem : Parcelable {
    @Parcelize
    data class Data(
        val certificate: Certificate,
        val title: String?,
        val coverFullPath: String?
    ) : CertificateListItem()

    @Parcelize
    object Placeholder : CertificateListItem()
}
