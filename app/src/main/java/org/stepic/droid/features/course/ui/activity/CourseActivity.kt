package org.stepic.droid.features.course.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_course.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Course

class CourseActivity : FragmentActivityBase() {

    private val course by lazy { intent.getParcelableExtra<Course>(AppConstants.KEY_COURSE_BUNDLE) }

    private val courseCoverSmallTarget by lazy {
        RoundedBitmapImageViewTarget(resources.getDimension(R.dimen.course_image_radius), courseCoverSmall)
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
                .bitmapTransform(CenterCrop(this), BlurTransformation(this))
                .into(courseCover)

        Glide.with(this)
                .load(config.baseUrl + course.cover)
                .asBitmap()
                .centerCrop()
                .into(courseCoverSmallTarget)

        courseTitle.text = course.title

        courseAppBar.addOnOffsetChangedListener { _, verticalOffset ->
            val ratio = Math.abs(verticalOffset).toFloat() / (courseCollapsingToolbar.height - courseToolbar.height)

            courseCover.alpha = 1f - ratio
            courseInfo.layoutParams = (courseInfo.layoutParams as LinearLayout.LayoutParams).apply {
                bottomMargin = (32 - 32 * ratio).toInt()
                height = (72 + (92 - 72) * ratio).toInt()
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