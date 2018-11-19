package org.stepik.android.view.course.ui.delegates

import android.app.Activity
import android.graphics.BitmapFactory
import android.support.design.widget.AppBarLayout
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_course.*
import kotlinx.android.synthetic.main.header_course.*
import org.stepic.droid.R
import org.stepic.droid.configuration.Config
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.domain.course.model.CourseHeaderData
import kotlin.math.roundToInt

class CourseHeaderDelegate(
        private val courseActivity: Activity,
        private val config: Config
) {
    private val courseCoverSmallTarget by lazy {
        RoundedBitmapImageViewTarget(courseActivity.resources.getDimension(R.dimen.course_image_radius), courseActivity.courseCoverSmall)
    }

    private val courseCoverSmallPlaceHolder by lazy {
        val resources = courseActivity.resources
        val coursePlaceholderBitmap = BitmapFactory.decodeResource(resources, R.drawable.general_placeholder)
        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, coursePlaceholderBitmap)
        circularBitmapDrawable.cornerRadius = resources.getDimension(R.dimen.course_image_radius)
        circularBitmapDrawable
    }

    init {
        initCollapsingAnimation()
        initVerified()
    }

    private fun initCollapsingAnimation() = with(courseActivity) {
        val courseInfoHeightExpanded = resources.getDimension(R.dimen.course_info_height_expanded)
        val courseInfoMarginExpanded = resources.getDimension(R.dimen.course_info_margin_expanded)

        courseAppBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val ratio = Math.abs(verticalOffset).toFloat() / (courseCollapsingToolbar.height - courseToolbar.height)
            val targetTranslation = courseInfoMarginExpanded - (courseToolbar.height - courseInfoHeightExpanded) / 2

            courseCover.alpha = 1f - ratio
            courseInfo.translationY = ratio * targetTranslation
        })
    }

    private fun initVerified() = with(courseActivity) {
        courseFeatured.setCompoundDrawables(start = R.drawable.ic_verified)
    }

    fun setCourse(courseHeaderData: CourseHeaderData) = with(courseActivity) {
        Glide.with(this)
                .load(config.baseUrl + courseHeaderData.cover)
                .placeholder(R.drawable.general_placeholder)
                .bitmapTransform(CenterCrop(this), BlurTransformation(this))
                .into(courseCover)

        Glide.with(this)
                .load(config.baseUrl + courseHeaderData.cover)
                .asBitmap()
                .placeholder(courseCoverSmallPlaceHolder)
                .centerCrop()
                .into(courseCoverSmallTarget)

        courseTitle.text = courseHeaderData.title

        courseRating.total = 5
        courseRating.progress = courseHeaderData.review.roundToInt()

        courseProgress.progress = courseHeaderData.progress / 100f
        courseProgressText.text = getString(R.string.percent_symbol, courseHeaderData.progress)

        courseLearnersCount.text = courseHeaderData.learnersCount.toString()

        courseFeatured.changeVisibility(courseHeaderData.isFeatured)
    }
}