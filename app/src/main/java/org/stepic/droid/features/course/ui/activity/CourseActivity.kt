package org.stepic.droid.features.course.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_course.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.features.course.ui.adapter.CoursePagerAdapter
import org.stepic.droid.features.course.ui.delegates.CourseHeaderDelegate
import org.stepic.droid.fonts.FontType
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Course
import uk.co.chrisjenx.calligraphy.TypefaceUtils

class CourseActivity : FragmentActivityBase() {
    companion object {
        private const val EXTRA_COURSE = "course"
        private const val EXTRA_COURSE_ID = "course_id"

        private const val EXTRA_AUTO_ENROLL = "auto_enroll"

        fun createIntent(context: Context, course: Course, autoEnroll: Boolean = false): Intent =
            Intent(context, CourseActivity::class.java)
                    .putExtra(EXTRA_COURSE, course)
                    .putExtra(EXTRA_AUTO_ENROLL, autoEnroll)

        fun createIntent(context: Context, courseId: Int): Intent =
                Intent(context, CourseActivity::class.java)
                        .putExtra(EXTRA_COURSE_ID, courseId)
    }

    private val course by lazy { intent.getParcelableExtra<Course>(AppConstants.KEY_COURSE_BUNDLE) }

    private lateinit var courseHeaderDelegate: CourseHeaderDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        setSupportActionBar(courseToolbar)
        val actionBar = this.supportActionBar
                ?: throw IllegalStateException("support action bar should be set")

        with(actionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        courseHeaderDelegate = CourseHeaderDelegate(this, config)
        courseHeaderDelegate.setCourse(course)

        initViewPager()
    }

    private fun initViewPager() {
        val lightFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.light))
        val regularFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.regular))

        coursePager.adapter = CoursePagerAdapter(course, this, supportFragmentManager)
        courseTabs.setupWithViewPager(coursePager)
        courseTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.let {
                    it.typeface = lightFont
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.let {
                    it.typeface = regularFont
                }
            }
        })

        for (i in 0 until courseTabs.tabCount) {
            val tab = courseTabs.getTabAt(i)
            tab?.customView = layoutInflater.inflate(R.layout.view_course_tab, null)
        }

        (courseTabs.getTabAt(courseTabs.selectedTabPosition)?.customView as? TextView)?.let {
            it.typeface = regularFont
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?) =
            if (item?.itemId == android.R.id.home) {
                onBackPressed()
                true
            } else {
                super.onOptionsItemSelected(item)
            }

    override fun applyTransitionPrev() {
        //no-op
    }
}