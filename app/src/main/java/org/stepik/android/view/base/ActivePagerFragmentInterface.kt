package org.stepik.android.view.base

import android.support.v4.app.Fragment

interface ActivePagerFragmentInterface {
    val activeFragments: Map<Int, Fragment>
}