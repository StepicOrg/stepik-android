package org.stepic.droid.util

import android.os.Binder
import android.os.Bundle
import androidx.core.app.BundleCompat
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Eases the Fragment.newInstance ceremony by marking the fragment's args with this delegate
 * Just write the property in newInstance and read it like any other property after the fragment has been created
 *
 * Inspired by Adam Powell, he mentioned it during his IO/17 talk about Kotlin
 */
class FragmentArgumentDelegate<T : Any> : ReadWriteProperty<Fragment, T> {
    private val bundleDelegate = BundleDelegate<T>()

    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val args = thisRef.arguments ?: throw IllegalStateException("Cannot read property ${property.name} if no arguments have been set")
        return bundleDelegate.getValue(args, property)
    }

    override operator fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        val args = thisRef.arguments ?: Bundle().also(thisRef::setArguments)
        bundleDelegate.setValue(args, property, value)
    }
}

class BundleDelegate<T : Any> : ReadWriteProperty<Bundle, T> {
    private var value: T? = null

    override operator fun getValue(thisRef: Bundle, property: KProperty<*>): T {
        if (value == null) {
            @Suppress("UNCHECKED_CAST")
            value = thisRef.get(property.name) as T
        }
        return value ?: throw IllegalStateException("Property ${property.name} could not be read")
    }

    override operator fun setValue(thisRef: Bundle, property: KProperty<*>, value: T) {
        val key = property.name

        when (value) {
            is String -> thisRef.putString(key, value)
            is Int -> thisRef.putInt(key, value)
            is Short -> thisRef.putShort(key, value)
            is Long -> thisRef.putLong(key, value)
            is Byte -> thisRef.putByte(key, value)
            is ByteArray -> thisRef.putByteArray(key, value)
            is Char -> thisRef.putChar(key, value)
            is CharArray -> thisRef.putCharArray(key, value)
            is CharSequence -> thisRef.putCharSequence(key, value)
            is Float -> thisRef.putFloat(key, value)
            is Bundle -> thisRef.putBundle(key, value)
            is Binder -> BundleCompat.putBinder(thisRef, key, value)
            is android.os.Parcelable -> thisRef.putParcelable(key, value)
            is java.io.Serializable -> thisRef.putSerializable(key, value)
            else -> throw IllegalStateException("Type ${value.javaClass.canonicalName} of property ${property.name} is not supported")
        }

        this.value = value
    }
}

fun <T : Any> Fragment.argument() = FragmentArgumentDelegate<T>()