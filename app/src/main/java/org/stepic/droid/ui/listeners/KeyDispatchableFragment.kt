package org.stepic.droid.ui.listeners

import android.view.KeyEvent

interface KeyDispatchableFragment {
    fun dispatchKeyEventInFragment(keyEvent: KeyEvent?) : Boolean
}
