package org.stepik.android.view.base.ui.extension

import android.support.v4.app.Fragment

inline fun <reified T> Fragment.parentOfType(): T? =
    parentOfType(T::class.java)

@Suppress("UNCHECKED_CAST")
fun <T> Fragment.parentOfType(klass: Class<T>): T? =
    parentFragment
        ?.let { parent ->
            if (klass.isAssignableFrom(parent.javaClass)) {
                parent as T
            } else {
                parent.parentOfType(klass)
            }
        }
        ?: activity?.let { activity ->
            if (klass.isAssignableFrom(activity.javaClass)) {
                activity as T
            } else {
                null
            }
        }