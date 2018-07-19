package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Certificate
import java.util.*

class CertificateViewItem(
        val certificateId: Long?,
        val title: String?,
        val coverFullPath: String?,
        val type: Certificate.Type?,
        val fullPath: String?,
        val grade: String?,
        val issueDate : Date?
): Parcelable {

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(certificateId ?: 0L)
        dest.writeString(title)
        dest.writeString(coverFullPath)
        dest.writeInt(type?.ordinal ?: -1)
        dest.writeString(fullPath)
        dest.writeString(grade)
        dest.writeSerializable(issueDate)
    }

    protected constructor(input: Parcel) : this(
            getCertificateIdByParcel(input),
            input.readString(),
            input.readString(),
            getCertificateTypeByParcel(input),
            input.readString(),
            input.readString(),
            input.readSerializable() as? Date
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
            return if (fromParcel == 0L) {
                null
            } else {
                fromParcel
            }
        }

        private fun getCertificateTypeByParcel(input: Parcel): Certificate.Type? {
            val temp = input.readInt()
            val localValues = Certificate.Type.values()
            return if (temp >= 0 && temp < localValues.size) {
                localValues[temp]
            } else {
                null
            }
        }
    }
}
