package org.stepic.droid.persistence.model

import android.os.Parcel
import android.os.Parcelable
import java.io.File

class StorageLocation(
        val path: File,
        val freeSpaceBytes: Long = path.freeSpace,
        val totalSpaceBytes: Long = path.totalSpace,
        val type: Type
) : Parcelable {
    enum class Type {
        APP_INTERNAL, // app's internal dir Context::getFilesDir, for this dir type relative paths should be used
        PRIMARY, // default external dir
        SECONDARY // additional external dirs
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StorageLocation

        if (type == Type.APP_INTERNAL && other == Type.APP_INTERNAL) return true
        if (path.canonicalPath != other.path.canonicalPath) return false

        return true
    }

    override fun hashCode(): Int = path.canonicalPath.hashCode()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(path)
        parcel.writeLong(freeSpaceBytes)
        parcel.writeLong(totalSpaceBytes)
        parcel.writeInt(type.ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<StorageLocation> {
        override fun createFromParcel(parcel: Parcel): StorageLocation = StorageLocation(
                parcel.readSerializable() as File,
                parcel.readLong(),
                parcel.readLong(),
                Type.values()[parcel.readInt()]
        )

        override fun newArray(size: Int): Array<StorageLocation?> =
                arrayOfNulls(size)
    }
}