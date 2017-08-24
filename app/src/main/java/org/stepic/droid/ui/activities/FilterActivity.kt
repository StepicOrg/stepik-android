package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment

import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.FilterFragment

class FilterActivity : SingleFragmentActivity() {

    companion object {
        val FILTER_TYPE_KEY = "filter_type_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.filter_title)
    }

    override fun createFragment(): Fragment {
        val filterCourseTypeCode = intent.getIntExtra(FILTER_TYPE_KEY, -1) // look at app constants
        return FilterFragment.newInstance(filterCourseTypeCode)
    }

    override fun applyTransitionPrev() {
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}
