package org.stepik.android.view.course_list.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_course_list_user.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.view.course_list.ui.adapter.viewpager.CourseListUserPagerAdapter

class CourseListUserActivity : FragmentActivityBase() {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, CourseListUserActivity::class.java)
    }

    private lateinit var courseListUserPagerAdapter: CourseListUserPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list_user)

        initCenteredToolbar(R.string.course_list_user_courses_title, showHomeButton = true)
        initViewPager()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViewPager() {
        courseListUserPagerAdapter = CourseListUserPagerAdapter(this, supportFragmentManager)
        userCourseListsPager.adapter = courseListUserPagerAdapter
        userCourseListsTabs.setupWithViewPager(userCourseListsPager)
    }
}