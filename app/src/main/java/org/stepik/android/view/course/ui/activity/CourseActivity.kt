package org.stepik.android.view.course.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_course.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepik.android.view.course.ui.adapter.CoursePagerAdapter
import org.stepik.android.view.course.ui.delegates.CourseHeaderDelegate
import org.stepic.droid.fonts.FontType
import org.stepik.android.model.Course
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.course.CourseView
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject

class CourseActivity : FragmentActivityBase(), CourseView {
    companion object {
        private const val EXTRA_COURSE = "course"
        private const val EXTRA_COURSE_ID = "course_id"

        private const val EXTRA_AUTO_ENROLL = "auto_enroll"

        private const val NO_ID = -1L

        fun createIntent(context: Context, course: Course, autoEnroll: Boolean = false): Intent =
            Intent(context, CourseActivity::class.java)
                    .putExtra(EXTRA_COURSE, course)
                    .putExtra(EXTRA_AUTO_ENROLL, autoEnroll)

        fun createIntent(context: Context, courseId: Int): Intent =
            Intent(context, CourseActivity::class.java)
                    .putExtra(EXTRA_COURSE_ID, courseId)
    }

    private var courseId: Long = NO_ID
    private lateinit var coursePresenter: CoursePresenter
    private lateinit var courseHeaderDelegate: CourseHeaderDelegate

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

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

        val course: Course? = savedInstanceState
                ?.getParcelable(EXTRA_COURSE)
                ?: intent.getParcelableExtra(EXTRA_COURSE)

        courseId = intent.getLongExtra(EXTRA_COURSE_ID, NO_ID)
                .takeIf { it != NO_ID }
                ?: course?.id
                ?: NO_ID

        injectComponent(courseId)
        coursePresenter = ViewModelProviders.of(this, viewModelFactory).get(CoursePresenter::class.java)

        initViewPager()

        if (course != null) {
            coursePresenter.onCourse(course)
        } else {
            coursePresenter.onCourseId(courseId)
        }
    }

    private fun injectComponent(courseId: Long) {
        App.componentManager()
            .courseComponent(courseId)
            .inject(this)
    }

    private fun releaseComponent(courseId: Long) {
        App.componentManager()
            .releaseCourseComponent(courseId)
    }

    override fun onStart() {
        super.onStart()
        coursePresenter.attachView(this)
    }

    override fun onStop() {
        coursePresenter.detachView(this)
        super.onStop()
    }

    private fun initViewPager() {
        val lightFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.light))
        val regularFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.regular))

        coursePager.adapter = CoursePagerAdapter(courseId, this, supportFragmentManager)
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

    override fun setState(state: CourseView.State) {
        when(state) {
            is CourseView.State.CourseLoaded ->
                courseHeaderDelegate.setCourse(state.course)

            is CourseView.State.EnrollmentProgress ->
                courseHeaderDelegate.setCourse(state.course)

            else -> TODO()
        }
    }

    override fun showEnrollmentError() {
        TODO()
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}