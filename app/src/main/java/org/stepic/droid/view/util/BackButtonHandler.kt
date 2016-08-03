package org.stepic.droid.view.util

interface BackButtonHandler {
    fun setBackClickListener(onBackClickListener: OnBackClickListener)
    fun removeBackClickListener(onBackClickListener: OnBackClickListener)
}
