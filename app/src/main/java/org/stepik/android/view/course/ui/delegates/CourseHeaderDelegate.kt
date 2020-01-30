package org.stepik.android.view.course.ui.delegates

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_course.*
import kotlinx.android.synthetic.main.header_course.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.util.PopupHelper
import org.stepic.droid.util.getAllQueryParameters
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course.routing.getCourseTabFromDeepLink
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import kotlin.math.abs

class CourseHeaderDelegate(
    private val courseActivity: Activity,
    private val analytic: Analytic,
    private val coursePresenter: CoursePresenter
) {
    var courseHeaderData: CourseHeaderData? = null
        set(value) {
            field = value
            value?.let(::setCourseData)
        }

    private var dropCourseMenuItem: MenuItem? = null
    private var shareCourseMenuItem: MenuItem? = null
    private var restorePurchaseCourseMenuItem: MenuItem? = null

    private val courseStatsDelegate = CourseStatsDelegate(courseActivity.courseStats)
    private val courseProgressDelegate = CourseProgressDelegate(courseActivity.courseProgress)

    private val viewStateDelegate = ViewStateDelegate<EnrollmentState>()

    init {
        initCollapsingAnimation()
        initActions()
        initViewStateDelegate()
    }

    private fun initCollapsingAnimation() {
        with(courseActivity) {
            courseAppBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                val ratio = abs(verticalOffset).toFloat() / (courseCollapsingToolbar.height - courseToolbar.height)
                courseToolbarScrim.alpha = ratio * 1.5f
            })
        }
    }

    private fun initActions() {
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

//            courseBuyInAppAction.setOnClickListener {
//                coursePresenter.purchaseCourse()
//            }
        }
    }

    private fun initViewStateDelegate() {
        with(courseActivity) {
            viewStateDelegate.addState<EnrollmentState.Enrolled>(courseContinueAction, courseProgress, courseProgressSeparator)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledFree>(courseEnrollAction)
            viewStateDelegate.addState<EnrollmentState.Pending>(courseEnrollmentProgress)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledWeb>(courseBuyInWebAction)
            // viewStateDelegate.addState<EnrollmentState.NotEnrolledInApp>(courseBuyInAppAction)
        }
    }

    private fun setCourseData(courseHeaderData: CourseHeaderData) =
        with(courseActivity) {
            val multi = MultiTransformation<Bitmap>(
                BlurTransformation(),
                CenterCrop()
            )
            Glide
                .with(this)
                .load(courseHeaderData.cover)
                .placeholder(R.drawable.general_placeholder)
                .apply(RequestOptions.bitmapTransform(multi))
                .into(courseCover)

            courseToolbarTitle.text = courseHeaderData.title

            // todo courseStatsDelegate.setStats()

            // todo courseProgressDelegate.setProgress(courseHeaderData.progress)

            with(courseHeaderData.enrollmentState) {
                viewStateDelegate.switchState(this)
                
                courseBuyInAppAction.isVisible = false // this is EnrollmentState.NotEnrolledInApp

//                if (this is EnrollmentState.NotEnrolledInApp) {
//                    courseBuyInAppAction.text = getString(R.string.course_payments_purchase_in_app, this.skuWrapper.sku.price)
//                }

                dropCourseMenuItem?.isVisible = this is EnrollmentState.Enrolled
                restorePurchaseCourseMenuItem?.isVisible = false // this is EnrollmentState.NotEnrolledInApp
            }

            shareCourseMenuItem?.isVisible = true
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
        restorePurchaseCourseMenuItem?.isVisible = false // courseHeaderData?.enrollmentState is EnrollmentState.NotEnrolledInApp
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
//                coursePresenter.restoreCoursePurchase()
                true
            }
            else ->
                false
        }
}