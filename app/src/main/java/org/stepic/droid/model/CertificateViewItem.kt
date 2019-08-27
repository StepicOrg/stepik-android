package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Certificate

class CertificateViewItem(
    val certificate: Certificate,
    val title: String?,
    val coverFullPath: String?
): Parcelable {

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(certificate, flags)
        dest.writeString(title)
        dest.writeString(coverFullPath)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CertificateViewItem> = object : Parcelable.Creator<CertificateViewItem> {
            override fun createFromParcel(source: Parcel): CertificateViewItem =
                CertificateViewItem(
                    source.readParcelable(Certificate::class.java.classLoader),
                    source.readString(),
                    source.readString()
                )

            override fun newArray(size: Int): Array<out CertificateViewItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
