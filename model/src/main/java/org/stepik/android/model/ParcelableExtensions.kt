package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable

fun Parcel.writeBoolean(value: Boolean) =
        writeByte(if (value) 1 else 0)

fun Parcel.readBoolean(): Boolean =
        readByte() != 0.toByte()

inline fun <reified T : Parcelable> Parcel.readParcelable(): T =
        readParcelable(T::class.java.classLoader)