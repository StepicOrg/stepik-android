package org.stepic.droid.ui.util

interface BackButtonHandler {
    fun setBackClickListener(onBackClickListener: OnBackClickListener)
    fun removeBackClickListener(onBackClickListener: OnBackClickListener)
}
