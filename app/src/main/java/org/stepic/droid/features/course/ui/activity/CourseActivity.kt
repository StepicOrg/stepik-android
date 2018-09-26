package org.stepic.droid.features.course.ui.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.AppCompatDrawableManager
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TabHost
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_course.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.features.course.ui.adapter.CoursePagerAdapter
import org.stepic.droid.fonts.FontType
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Course
import uk.co.chrisjenx.calligraphy.TypefaceUtils

class CourseActivity : FragmentActivityBase() {

    private val course by lazy { intent.getParcelableExtra<Course>(AppConstants.KEY_COURSE_BUNDLE) }

    private val courseCoverSmallTarget by lazy {
        RoundedBitmapImageViewTarget(resources.getDimension(R.dimen.course_image_radius), courseCoverSmall)
    }

    private val courseCoverSmallPlaceHolder by lazy {
        val coursePlaceholderBitmap = BitmapFactory.decodeResource(resources, R.drawable.general_placeholder)
        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, coursePlaceholderBitmap)
        circularBitmapDrawable.cornerRadius = resources.getDimension(R.dimen.course_image_radius)
        circularBitmapDrawable
    }

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

        Glide.with(this)
                .load(config.baseUrl + course.cover)
                .placeholder(R.drawable.general_placeholder)
                .bitmapTransform(CenterCrop(this), BlurTransformation(this))
                .into(courseCover)

        Glide.with(this)
                .load(config.baseUrl + course.cover)
                .asBitmap()
                .placeholder(courseCoverSmallPlaceHolder)
                .centerCrop()
                .into(courseCoverSmallTarget)

        courseTitle.text = course.title

        courseRating.total = 5
        courseRating.progress = 3

        courseProgress.progress = .77f
        courseProgressText.text = "77%"


        courseLearnersCount.text = course.learnersCount.toString()


        initVerified()
        initCollapsingAnimation()
        initViewPager()
    }

    private fun initViewPager() {
        val lightFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.light))
        val regularFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.regular))

        coursePager.adapter = CoursePagerAdapter(this, supportFragmentManager)
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

    private fun initVerified() {
        val verifiedDrawable = AppCompatDrawableManager.get().getDrawable(this, R.drawable.ic_verified)
        courseVerified.setCompoundDrawablesWithIntrinsicBounds(verifiedDrawable, null, null, null)
    }

    private fun initCollapsingAnimation() {
        val courseInfoHeightExpanded = resources.getDimension(R.dimen.course_info_height_expanded)
        val courseInfoMarginExpanded = resources.getDimension(R.dimen.course_info_margin_expanded)

        courseAppBar.addOnOffsetChangedListener { _, verticalOffset ->
            val ratio = Math.abs(verticalOffset).toFloat() / (courseCollapsingToolbar.height - courseToolbar.height)

            courseCover.alpha = 1f - ratio
            courseInfo.layoutParams = (courseInfo.layoutParams as LinearLayout.LayoutParams).apply {
                bottomMargin = (courseInfoMarginExpanded * (1 - ratio)).toInt()
                height = (courseInfoHeightExpanded + (courseToolbar.height - courseInfoHeightExpanded) * ratio).toInt()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.share_menu, menu)
        return super.onCreateOptionsMenu(menu)
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