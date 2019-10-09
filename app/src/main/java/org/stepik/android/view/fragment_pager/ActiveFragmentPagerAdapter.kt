package org.stepik.android.view.fragment_pager

import androidx.fragment.app.Fragment

interface ActiveFragmentPagerAdapter {
    val activeFragments: Map<Int, Fragment>
}