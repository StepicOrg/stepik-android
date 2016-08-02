package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

class CertificateViewItem(val certificateId: Long?,
                          val title: String,
                          val coverFullPath: String?,
                          val type: CertificateType?,
                          val fullPath: String?,
                          val grade: String?) : Parcelable {

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(certificateId ?: 0L)
        dest.writeString(title)
        dest.writeString(coverFullPath)
        dest.writeInt(type?.ordinal ?: -1)
        dest.writeString(fullPath)
        dest.writeString(grade)
    }

    protected constructor(input: Parcel) : this(
            getCertificateIdByParcel(input),
            input.readString(),
            input.readString(),
            getCertificateTypeByParcel(input),
            input.readString(),
            input.readString()
    )


    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CertificateViewItem> = object : Parcelable.Creator<CertificateViewItem> {
            override fun createFromParcel(source: Parcel): CertificateViewItem? {
                return CertificateViewItem(source)
            }

            override fun newArray(size: Int): Array<out CertificateViewItem?> {
                return arrayOfNulls(size)
            }
        }

        private fun getCertificateIdByParcel(input: Parcel): Long? {
            val fromParcel = input.readLong()
            if (fromParcel == 0L) {
                return null
            } else {
                return fromParcel
            }
        }

        private fun getCertificateTypeByParcel(input: Parcel): CertificateType? {
            val temp = input.readInt()
            val localValues = CertificateType.values()
            if (temp >= 0 && temp < localValues.size) {
                return localValues[temp]
            } else {
                return null
            }
        }
    }
}
