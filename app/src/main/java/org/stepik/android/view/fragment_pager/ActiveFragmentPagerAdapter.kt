package org.stepik.android.view.fragment_pager

import android.support.v4.app.Fragment

interface ActiveFragmentPagerAdapter {
    val activeFragments: Map<Int, Fragment>
}