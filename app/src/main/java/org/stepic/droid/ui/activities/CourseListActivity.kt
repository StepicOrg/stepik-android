package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.fragments.MyCoursesFragment
import org.stepic.droid.ui.fragments.PopularCoursesAloneFragment

class CourseListActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment {
        val info = intent.getParcelableExtra<CoursesCarouselInfo>(COURSE_LIST_INFO_KEY)
        if (info.table == Table.enrolled) {
            return MyCoursesFragment.newInstance()
        } else if (info.table == Table.featured) {
            return PopularCoursesAloneFragment.newInstance()
        } else {
            throw IllegalStateException("course lists are not supported yet")
        }
    }

    companion object {
        const val COURSE_LIST_INFO_KEY = "CourseListInfoKey"
    }

    override fun applyTransitionPrev() {
        //no-op
    }
}
