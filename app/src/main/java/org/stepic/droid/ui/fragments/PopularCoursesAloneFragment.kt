package org.stepic.droid.ui.fragments

import android.view.Menu
import android.view.MenuInflater
import org.stepic.droid.R

open class PopularCoursesAloneFragment : FindCoursesFragment() {
    companion object {
        fun newInstance(): PopularCoursesAloneFragment = PopularCoursesAloneFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        //no-op
    }

    override fun getTitle(): String = getString(R.string.popular_courses_title)
}
