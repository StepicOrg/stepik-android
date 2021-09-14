package org.stepik.android.view.debug.ui.fragment

import androidx.fragment.app.Fragment

interface DebugMenu {
    companion object {
        const val TAG = "DebugFragment"
        fun newInstance(): Fragment = DebugFragment.newInstance()
    }
}