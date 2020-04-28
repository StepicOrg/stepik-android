package org.stepik.android.view.course.ui.delegates

import android.app.Activity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import org.stepic.droid.analytic.experiments.CoursePurchasePriceSplitTest
import org.stepic.droid.ui.util.PopupHelper
import org.stepic.droid.util.getAllQueryParameters
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.view.base.ui.extension.ColorExtensions
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course.routing.getCourseTabFromDeepLink
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import kotlin.math.abs

class CourseHeaderDelegate(
    private val courseActivity: Activity,
    private val analytic: Analytic,
    private val coursePresenter: CoursePresenter,
    private val coursePurchasePriceSplitTest: CoursePurchasePriceSplitTest,
    onSubmissionCountClicked: () -> Unit,
    isLocalSubmissionsEnabled: Boolean
) {
    var courseHeaderData: CourseHeaderData? = null
        set(value) {
            field = value
            value?.let(::setCourseData)
        }

    private var dropCourseMenuItem: MenuItem? = null
    private var shareCourseMenuItem: MenuItem? = null
    private var favoriteCourseMenuItem: MenuItem? = null
    private var archiveCourseMenuItem: MenuItem? = null
    private var restorePurchaseCourseMenuItem: MenuItem? = null

    private val courseStatsDelegate = CourseStatsDelegate(courseActivity.courseStats)
    private val courseProgressDelegate = CourseProgressDelegate(courseActivity.courseProgress, onSubmissionCountClicked, isLocalSubmissionsEnabled)

    private val courseCollapsingToolbar = courseActivity.courseCollapsingToolbar

    private val viewStateDelegate = ViewStateDelegate<EnrollmentState>()

    init {
        initCollapsingAnimation()
        initActions()
        initViewStateDelegate()
    }

    private fun initCollapsingAnimation() {
        with(courseActivity) {
            courseToolbarScrim.setBackgroundColor(ColorExtensions.colorSurfaceWithElevationOverlay(courseCollapsingToolbar.context, 4))

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

                courseHeaderData?.let { headerData ->
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Course.BUY_COURSE_PRESSED, mapOf(
                        AmplitudeAnalytic.Course.Params.COURSE to headerData.courseId,
                        AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.COURSE_SCREEN
                    ))
                }
            }

//            courseBuyInAppAction.setOnClickListener {
//                coursePresenter.purchaseCourse()
//            }
        }
    }

    private fun initViewStateDelegate() {
        with(courseActivity) {
            viewStateDelegate.addState<EnrollmentState.Enrolled>(courseContinueAction)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledFree>(courseEnrollAction)
            viewStateDelegate.addState<EnrollmentState.Pending>(courseEnrollmentProgress)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledWeb>(courseBuyInWebAction)
            // viewStateDelegate.addState<EnrollmentState.NotEnrolledInApp>(courseBuyInAppAction)
        }
    }

    private fun setCourseData(courseHeaderData: CourseHeaderData) =
        with(courseActivity) {
            val multi = MultiTransformation(BlurTransformation(), CenterCrop())
            Glide
                .with(this)
                .load(courseHeaderData.cover)
                .placeholder(R.drawable.general_placeholder)
                .apply(RequestOptions.bitmapTransform(multi))
                .into(courseCover)

            courseToolbarTitle.text = courseHeaderData.title

            val isNeedShowProgress = courseHeaderData.stats.progress != null
            courseProgress.isVisible = isNeedShowProgress
            courseProgressSeparator.isVisible = isNeedShowProgress
            courseStats.isVisible = !isNeedShowProgress

            if (courseHeaderData.stats.progress != null) {
                courseProgressDelegate.setProgress(courseHeaderData.stats.progress)
                courseProgressDelegate.setSolutionsCount(courseHeaderData.localSubmissionsCount)
            } else {
                courseStatsDelegate.setStats(courseHeaderData.stats)
            }

            courseBuyInWebAction.text = if (coursePurchasePriceSplitTest.currentGroup.isPriceVisible && courseHeaderData.course.displayPrice != null) {
                getString(R.string.course_payments_purchase_in_web_with_price, courseHeaderData.course.displayPrice)
            } else {
                getString(R.string.course_payments_purchase_in_web)
            }

            with(courseHeaderData.stats.enrollmentState) {
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
        favoriteCourseMenuItem = menu.findItem(R.id.favorite_course)
        favoriteCourseMenuItem?.isVisible = courseHeaderData != null
        favoriteCourseMenuItem?.isEnabled = true
        favoriteCourseMenuItem?.title = if (courseHeaderData?.userCourse?.isFavorite == true) {
            courseActivity.getString(R.string.course_remove_from_favorites)
        } else {
            courseActivity.getString(R.string.course_add_to_favorites)
        }

        archiveCourseMenuItem = menu.findItem(R.id.archive_course)
        archiveCourseMenuItem?.isVisible = courseHeaderData != null
        archiveCourseMenuItem?.isEnabled = true
        archiveCourseMenuItem?.title = if (courseHeaderData?.userCourse?.isArchived == true) {
            courseActivity.getString(R.string.course_remove_from_archive)
        } else {
            courseActivity.getString(R.string.course_add_to_archive)
        }

        dropCourseMenuItem = menu.findItem(R.id.drop_course)
        dropCourseMenuItem?.isVisible = courseHeaderData?.stats?.enrollmentState == EnrollmentState.Enrolled
        val dropCourseMenuItemSpan = SpannableString(dropCourseMenuItem?.title)
        dropCourseMenuItemSpan.setSpan(ForegroundColorSpan(courseCollapsingToolbar.context.resolveColorAttribute(R.attr.colorError)), 0, dropCourseMenuItemSpan.length, 0)
        dropCourseMenuItem?.title = dropCourseMenuItemSpan

        shareCourseMenuItem = menu.findItem(R.id.share_course)
        shareCourseMenuItem?.isVisible = courseHeaderData != null

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
            R.id.favorite_course -> {
                favoriteCourseMenuItem?.isEnabled = false
                coursePresenter.toggleFavorite()
                true
            }
            R.id.archive_course -> {
                archiveCourseMenuItem?.isEnabled = false
                coursePresenter.toggleArchive()
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