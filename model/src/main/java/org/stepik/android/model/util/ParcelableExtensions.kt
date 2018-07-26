package org.stepik.android.model.util

import android.os.Parcel
import android.os.Parcelable
import java.util.*

fun Parcel.writeBoolean(value: Boolean) =
        writeByte(if (value) 1 else 0)

fun Parcel.readBoolean(): Boolean =
        readByte() != 0.toByte()


private fun getParcelableWriter(flags: Int): Parcel.(Parcelable) -> Unit = { writeParcelable(it, flags) }
private fun <T: Parcelable> getParcelableReader(classLoader: ClassLoader): Parcel.() -> T = { readParcelable(classLoader) }

fun <K : Parcelable, V : Parcelable> Parcel.writeMapCustom(map: Map<K, V>, flags: Int) =
        writeMap(map, getParcelableWriter(flags), getParcelableWriter(flags))

fun <V : Parcelable> Parcel.writeMapCustomString(map: Map<String, V>, flags: Int) =
        writeMap(map, Parcel::writeString, getParcelableWriter(flags))

inline fun <K, V> Parcel.writeMap(map: Map<K, V>, writeKey: Parcel.(K) -> Unit, writeVal: Parcel.(V) -> Unit) {
    writeInt(map.size)
    for ((key, value) in map.entries) {
        writeKey(key)
        writeVal(value)
    }
}

inline fun <K, V> Parcel.readMap(readKey: Parcel.() -> K, readVal: Parcel.() -> V): Map<K, V> {
    val size = readInt()
    val map = HashMap<K, V>(size)
    for (i in 0 until size) {
        val key = readKey()
        val value = readVal()
        map[key] = value
    }
    return map
}

fun <K : Parcelable, V : Parcelable> Parcel.readMapCustom(classLoaderKey: ClassLoader, classLoaderValue: ClassLoader): Map<K, V> =
        readMap(getParcelableReader(classLoaderKey), getParcelableReader(classLoaderValue))

fun <V : Parcelable> Parcel.readMapCustomString(classLoaderValue: ClassLoader): Map<String, V> =
        readMap(Parcel::readString, getParcelableReader(classLoaderValue))


internal const val NO_VALUE = -1L

fun Parcel.writeDate(value: Date?) =
        writeLong(value?.time ?: NO_VALUE)

fun Parcel.readDate(): Date? =
        readLong().takeIf { it != NO_VALUE }?.let(::Date)