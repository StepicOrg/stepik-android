package org.stepik.android.view.course_list.ui.activity

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.view.course_list.ui.fragment.CourseListUserFragment

class CourseListUserActivity : SingleFragmentActivity() {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, CourseListUserActivity::class.java)
    }
    override fun createFragment(): Fragment =
        CourseListUserFragment.newInstance()
}