package org.stepik.android.view.base.ui.extension

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

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

inline fun <reified T : ViewModel> Fragment.viewModel(factory: ViewModelProvider.Factory? = null): T =
    ViewModelProviders.of(this, factory).get(T::class.java)