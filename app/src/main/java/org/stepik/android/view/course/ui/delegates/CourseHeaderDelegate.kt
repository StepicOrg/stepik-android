package org.stepik.android.view.course.ui.delegates

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_course.*
import kotlinx.android.synthetic.main.header_course.*
import kotlinx.android.synthetic.main.header_course_placeholder.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.util.PopupHelper
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.getAllQueryParameters
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course.routing.getCourseTabFromDeepLink
import kotlin.math.abs
import kotlin.math.roundToInt

class CourseHeaderDelegate(
    private val courseActivity: Activity,
    private val analytic: Analytic,
    private val coursePresenter: CoursePresenter
) {
    companion object {
        private const val MIN_FEATURED_READINESS = 0.9
    }

    var courseHeaderData: CourseHeaderData? = null
        set(value) {
            field = value
            value?.let(::setCourseData)
        }

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

    private var dropCourseMenuItem: MenuItem? = null
    private var shareCourseMenuItem: MenuItem? = null
    private var restorePurchaseCourseMenuItem: MenuItem? = null

    init {
        initCollapsingAnimation()
        initCompoundDrawables()
        initActions()
    }

    private fun initCollapsingAnimation() =
        with(courseActivity) {
            val courseInfoHeightExpanded = resources.getDimension(R.dimen.course_info_height_expanded)
            val courseInfoMarginExpanded = resources.getDimension(R.dimen.course_info_margin_expanded)

            courseAppBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                val ratio = abs(verticalOffset).toFloat() / (courseCollapsingToolbar.height - courseToolbar.height)
                val targetTranslation = courseInfoMarginExpanded - (courseToolbar.height - courseInfoHeightExpanded) / 2

                courseCover.alpha = 1f - ratio
                courseInfo.translationY = ratio * targetTranslation
                courseInfoPlaceholder.translationY = ratio * targetTranslation
            })
        }

    private fun initCompoundDrawables() =
        with(courseActivity) {
            courseFeatured.setCompoundDrawables(start = R.drawable.ic_verified)

            val learnersCountDrawable = AppCompatResources
                .getDrawable(this, R.drawable.ic_learners)
                ?.let(DrawableCompat::wrap)
                ?: return@with
            DrawableCompat.setTint(learnersCountDrawable, ContextCompat.getColor(this,  android.R.color.white))
            courseLearnersCount.setCompoundDrawablesWithIntrinsicBounds(learnersCountDrawable, null, null, null)
        }

    private fun initActions() =
        with(courseActivity) {
            courseEnrollAction.setOnClickListener {
                coursePresenter.enrollCourse()

                courseHeaderData?.let { headerData ->
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Course.JOINED, mapOf(
                        AmplitudeAnalytic.Course.Params.COURSE to headerData.courseId,
                        AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.PREVIEW
                    ))
                }
            }

            courseContinueAction.setOnClickListener {
                coursePresenter.continueLearning()

                courseHeaderData?.let { headerData ->
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
                        AmplitudeAnalytic.Course.Params.COURSE to headerData.courseId,
                        AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.COURSE_SCREEN
                    ))
                }
            }

            courseBuyInWebAction.setOnClickListener {
                val queryParams = courseActivity
                    .intent
                    ?.takeIf { it.getCourseTabFromDeepLink() == CourseScreenTab.PAY }
                    ?.data
                    ?.getAllQueryParameters()

                coursePresenter.openCoursePurchaseInWeb(queryParams)
            }

            courseBuyInAppAction.setOnClickListener {
                coursePresenter.purchaseCourse()
            }
        }

    private fun setCourseData(courseHeaderData: CourseHeaderData) =
        with(courseActivity) {
            val multi = MultiTransformation<Bitmap>(
                BlurTransformation(),
                CenterCrop()
            )
            Glide.with(this)
                    .load(courseHeaderData.cover)
                    .placeholder(R.drawable.general_placeholder)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(courseCover)

            Glide.with(this)
                    .asBitmap()
                    .load(courseHeaderData.cover)
                    .placeholder(courseCoverSmallPlaceHolder)
                    .centerCrop()
                    .into(courseCoverSmallTarget)

            courseTitle.text = courseHeaderData.title

            courseRating.total = 5
            courseRating.progress = courseHeaderData.review.roundToInt()
            courseRating.isVisible = courseHeaderData.review > 0

            val isNeedShowProgress = courseHeaderData.progress != null && courseHeaderData.progress.cost > 0
            courseProgress.isVisible = isNeedShowProgress
            courseProgressText.isVisible = isNeedShowProgress

            if (isNeedShowProgress) {
                val score = courseHeaderData
                    .progress
                    ?.score
                    ?.toFloatOrNull()
                    ?.toLong()
                    ?: 0L

                val cost = courseHeaderData
                    .progress
                    ?.cost
                    ?: 0L

                courseProgress.progress = (score * 100 / cost) / 100f
                courseProgressText.text = getString(R.string.course_content_text_progress, score, cost)
            }

            courseLearnersCount.text = courseHeaderData.learnersCount.toString()
            courseFeatured.isVisible = courseHeaderData.readiness > MIN_FEATURED_READINESS

            with(courseHeaderData.enrollmentState) {
                courseEnrollAction.isVisible = this is EnrollmentState.NotEnrolledFree
                courseEnrollmentProgress.isVisible = this is EnrollmentState.Pending
                courseContinueAction.isVisible = this is EnrollmentState.Enrolled
                courseBuyInWebAction.isVisible = this is EnrollmentState.NotEnrolledWeb
                courseBuyInAppAction.isVisible = this is EnrollmentState.NotEnrolledInApp

                if (this is EnrollmentState.NotEnrolledInApp) {
                    courseBuyInAppAction.text = getString(R.string.course_payments_purchase_in_app, this.skuWrapper.sku.price)
                }

                dropCourseMenuItem?.isVisible = this is EnrollmentState.Enrolled
                restorePurchaseCourseMenuItem?.isVisible = this is EnrollmentState.NotEnrolledInApp
            }

            shareCourseMenuItem?.isVisible = true

            courseToolbarConstraint.doOnPreDraw {
                val offset = maxOf(courseToolbar.height, courseToolbar.width - courseToolbarConstraint.right)
                courseInfo.layoutParams = (courseInfo.layoutParams as LinearLayout.LayoutParams)
                    .apply {
                        leftMargin = offset
                        rightMargin = offset
                    }

                courseInfoPlaceholder.layoutParams = (courseInfoPlaceholder.layoutParams as LinearLayout.LayoutParams)
                    .apply {
                        leftMargin = offset
                        rightMargin = offset
                    }
            }
        }

    fun showCourseShareTooltip() {
        val menuItemView = courseActivity
            .courseToolbar
            .findViewById<View>(R.id.share_course)
            ?: return

        PopupHelper
            .showPopupAnchoredToView(
                courseActivity,
                menuItemView,
                courseActivity.getString(R.string.course_share_description),
                theme = PopupHelper.PopupTheme.LIGHT,
                cancelableOnTouchOutside = true,
                withArrow = true
            )
    }

    fun onOptionsMenuCreated(menu: Menu) {
        dropCourseMenuItem = menu.findItem(R.id.drop_course)
        dropCourseMenuItem?.isVisible = courseHeaderData?.enrollmentState == EnrollmentState.Enrolled

        shareCourseMenuItem = menu.findItem(R.id.share_course)
        shareCourseMenuItem?.let { menuItem ->
            menuItem.icon
                ?.mutate()
                ?.setColorFilter(ContextCompat.getColor(courseActivity, R.color.white), PorterDuff.Mode.SRC_IN)

            menuItem.isVisible = courseHeaderData != null
        }

        restorePurchaseCourseMenuItem = menu.findItem(R.id.restore_purchase)
        restorePurchaseCourseMenuItem?.isVisible = courseHeaderData?.enrollmentState is EnrollmentState.NotEnrolledInApp
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.drop_course -> {
                coursePresenter.dropCourse()

                courseHeaderData?.let { headerData ->
                    analytic.reportAmplitudeEvent(
                        AmplitudeAnalytic.Course.UNSUBSCRIBED, mapOf(
                            AmplitudeAnalytic.Course.Params.COURSE to headerData.courseId
                        )
                    )
                }
                true
            }
            R.id.share_course -> {
                coursePresenter.shareCourse()
                true
            }
            R.id.restore_purchase -> {
                coursePresenter.restoreCoursePurchase()
                true
            }
            else ->
                false
        }
}