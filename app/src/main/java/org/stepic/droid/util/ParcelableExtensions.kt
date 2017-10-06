package org.stepic.droid.util

import android.os.Parcel
import android.os.Parcelable


fun <K : Parcelable, V : Parcelable> Parcel.writeMapCustom(map: Map<K, V>, flags: Int) {
    this.writeInt(map.size)
    for (entry in map.entries) {
        this.writeParcelable(entry.key, flags)
        this.writeParcelable(entry.value, flags)
    }
}

fun <K : Parcelable, V : Parcelable> Parcel.readMapCustom(classLoaderKey: ClassLoader, classLoaderValue: ClassLoader): Map<K, V> {
    val size = this.readInt()
    val map = HashMap<K, V>(size)
    for (i in 0 until size) {
        val key = this.readParcelable<K>(classLoaderKey)
        val value = this.readParcelable<V>(classLoaderValue)
        map.put(key, value)
    }
    return map
}