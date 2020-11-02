package org.stepik.android.view.course_list.ui.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.view.course_list.ui.fragment.CourseListVisitedFragment

class CourseListVisitedActivity : SingleFragmentActivity() {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, CourseListVisitedActivity::class.java)
    }

    override fun createFragment(): Fragment =
        CourseListVisitedFragment.newInstance()

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}