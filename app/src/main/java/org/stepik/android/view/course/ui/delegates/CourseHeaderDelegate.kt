package org.stepik.android.view.course.ui.delegates

import android.app.Activity
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_course.*
import kotlinx.android.synthetic.main.header_course.*
import kotlinx.android.synthetic.main.view_discounted_purchase_button.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.DiscountButtonAppearanceSplitTest
import org.stepic.droid.ui.util.PopupHelper
import org.stepic.droid.util.getAllQueryParameters
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_payments.model.PromoCode
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.user_courses.model.UserCourseAction
import org.stepik.android.view.base.ui.extension.ColorExtensions
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.core.model.safeCast
import kotlin.math.abs

class CourseHeaderDelegate(
    private val courseActivity: Activity,
    private val analytic: Analytic,
    private val coursePresenter: CoursePresenter,
    private val discountButtonAppearanceSplitTest: DiscountButtonAppearanceSplitTest,
    onSubmissionCountClicked: () -> Unit,
    isLocalSubmissionsEnabled: Boolean
) {
    companion object {
        private val CourseHeaderData.enrolledState: EnrollmentState.Enrolled?
            get() = stats.enrollmentState.safeCast<EnrollmentState.Enrolled>()

        private const val RUB_FORMAT = "RUB"
        private const val USD_FORMAT = "USD"
    }

    var courseHeaderData: CourseHeaderData? = null
        set(value) {
            field = value
            value?.let(::setCourseData)
        }

    private var dropCourseMenuItem: MenuItem? = null
    private var shareCourseMenuItem: MenuItem? = null
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

            courseBuyInWebAction.setOnClickListener { buyInWebAction() }
            courseBuyInWebActionDiscounted.setOnClickListener { buyInWebAction() }

            courseBuyInAppAction.setOnClickListener {
                coursePresenter.purchaseCourse()
            }

            courseTryFree.setOnClickListener {
                val lessonId = courseHeaderData
                    ?.course
                    ?.courseOptions
                    ?.coursePreview
                    ?.previewLessonId
                    ?: return@setOnClickListener

                coursePresenter.tryLessonFree(lessonId)
            }
        }
    }

    private fun buyInWebAction() {
        val queryParams = courseActivity
            .intent
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

    private fun initViewStateDelegate() {
        with(courseActivity) {
            viewStateDelegate.addState<EnrollmentState.Enrolled>(courseContinueAction)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledFree>(courseEnrollAction)
            viewStateDelegate.addState<EnrollmentState.Pending>(courseEnrollmentProgress)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledWeb>(purchaseContainer)
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

            courseBuyInWebAction.text = if (courseHeaderData.course.displayPrice != null) {
                if (courseHeaderData.promoCode.price != -1L) {
                    setupPurchaseButtonText(courseHeaderData.course.displayPrice as String, courseHeaderData.promoCode)
                } else {
                    getString(R.string.course_payments_purchase_in_web_with_price, courseHeaderData.course.displayPrice)
                }
            } else {
                getString(R.string.course_payments_purchase_in_web)
            }

            courseBuyInWebActionDiscountedNewPrice.text = getString(R.string.course_payments_purchase_in_web_with_price, formatPromoDisplayPrice(courseHeaderData.promoCode))
            courseBuyInWebActionDiscountedOldPrice.text = SpannableString(courseHeaderData.course.displayPrice).apply { setSpan(StrikethroughSpan(), 0, courseHeaderData.course.displayPrice?.length ?: 0, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }

            if (courseHeaderData.course.displayPrice != null && courseHeaderData.promoCode.price != -1L) {
                when (discountButtonAppearanceSplitTest.currentGroup) {
                    DiscountButtonAppearanceSplitTest.Group.DiscountTransparent -> {
                        courseBuyInWebAction.isVisible = false
                        courseBuyInWebActionDiscounted.isVisible = true
                    }
                    DiscountButtonAppearanceSplitTest.Group.DiscountGreen -> {
                        courseBuyInWebAction.isVisible = true
                        courseBuyInWebActionDiscounted.isVisible = false
                        ViewCompat.setBackgroundTintList(courseBuyInWebAction, ContextCompat.getColorStateList(courseActivity, R.color.color_overlay_green))
                    }
                    DiscountButtonAppearanceSplitTest.Group.DiscountPurple -> {
                        courseBuyInWebAction.isVisible = true
                        courseBuyInWebActionDiscounted.isVisible = false
                        ViewCompat.setBackgroundTintList(courseBuyInWebAction, ContextCompat.getColorStateList(courseActivity, R.color.color_overlay_violet))
                    }
                }
            } else {
                courseBuyInWebAction.isVisible = true
                courseBuyInWebActionDiscounted.isVisible = false
            }

            with(courseHeaderData.stats.enrollmentState) {
                viewStateDelegate.switchState(this)

                courseBuyInAppAction.isVisible = this is EnrollmentState.NotEnrolledInApp

                if (this is EnrollmentState.NotEnrolledInApp) {
                    courseBuyInAppAction.text = getString(R.string.course_payments_purchase_in_app, this.skuWrapper.sku.price)
                }

                dropCourseMenuItem?.isVisible = this is EnrollmentState.Enrolled
                restorePurchaseCourseMenuItem?.isVisible = false // this is EnrollmentState.NotEnrolledInApp
            }

            courseTryFree.isVisible = courseHeaderData.course.courseOptions?.coursePreview?.previewLessonId != null &&
                    courseHeaderData.course.enrollment == 0L &&
                    courseHeaderData.course.isPaid &&
                    (courseHeaderData.stats.enrollmentState is EnrollmentState.NotEnrolledInApp || courseHeaderData.stats.enrollmentState is EnrollmentState.NotEnrolledWeb)

            shareCourseMenuItem?.isVisible = true
        }

    private fun setupPurchaseButtonText(originalDisplayPrice: String, promoCode: PromoCode): Spannable {
        val promoDisplayPrice = formatPromoDisplayPrice(promoCode)
        val spanString = courseActivity.getString(R.string.course_payments_purchase_in_web_with_price_promo, promoDisplayPrice, originalDisplayPrice)
        return SpannableString(spanString).apply {
            setSpan(RelativeSizeSpan(0.9f), spanString.length - promoDisplayPrice.length - 1, spanString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StrikethroughSpan(), spanString.length - promoDisplayPrice.length - 1, spanString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun formatPromoDisplayPrice(promoCode: PromoCode): String =
        when (promoCode.currencyCode) {
            RUB_FORMAT ->
                courseActivity.getString(R.string.rub_format, promoCode.price)
            USD_FORMAT ->
                courseActivity.getString(R.string.usd_format, promoCode.price)
            else ->
                "${promoCode.price} ${promoCode.currencyCode}"
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
        val userCourseState = courseHeaderData?.enrolledState

        menu.findItem(R.id.favorite_course)
            ?.let { favoriteCourseMenuItem ->

                favoriteCourseMenuItem.isVisible = userCourseState != null
                favoriteCourseMenuItem.isEnabled = userCourseState?.isUserCourseUpdating == false
                favoriteCourseMenuItem.title =
                    if (userCourseState?.userCourse?.isFavorite == true) {
                        courseActivity.getString(R.string.course_action_favorites_remove)
                    } else {
                        courseActivity.getString(R.string.course_action_favorites_add)
                    }
            }

        menu.findItem(R.id.archive_course)
            ?.let { archiveCourseMenuItem ->
                archiveCourseMenuItem.isVisible = userCourseState != null
                archiveCourseMenuItem.isEnabled = userCourseState?.isUserCourseUpdating == false
                archiveCourseMenuItem.title =
                    if (userCourseState?.userCourse?.isArchived == true) {
                        courseActivity.getString(R.string.course_action_archive_remove)
                    } else {
                        courseActivity.getString(R.string.course_action_archive_add)
                    }
            }

        dropCourseMenuItem = menu.findItem(R.id.drop_course)
        dropCourseMenuItem?.isVisible = courseHeaderData?.stats?.enrollmentState is EnrollmentState.Enrolled
        val dropCourseMenuItemSpan = SpannableString(dropCourseMenuItem?.title)
        dropCourseMenuItemSpan.setSpan(ForegroundColorSpan(courseCollapsingToolbar.context.resolveColorAttribute(R.attr.colorError)), 0, dropCourseMenuItemSpan.length, 0)
        dropCourseMenuItem?.title = dropCourseMenuItemSpan

        shareCourseMenuItem = menu.findItem(R.id.share_course)
        shareCourseMenuItem?.isVisible = courseHeaderData != null

        restorePurchaseCourseMenuItem = menu.findItem(R.id.restore_purchase)
        restorePurchaseCourseMenuItem?.isVisible = courseHeaderData?.stats?.enrollmentState is EnrollmentState.NotEnrolledInApp
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
                courseHeaderData?.enrolledState?.let {
                    val action =
                        if (it.userCourse.isFavorite) {
                            UserCourseAction.REMOVE_FAVORITE
                        } else {
                            UserCourseAction.ADD_FAVORITE
                        }
                    coursePresenter.toggleUserCourse(action)
                }
                true
            }
            R.id.archive_course -> {
                courseHeaderData?.enrolledState?.let {
                    val action =
                        if (it.userCourse.isArchived) {
                            UserCourseAction.REMOVE_ARCHIVE
                        } else {
                            UserCourseAction.ADD_ARCHIVE
                        }
                    coursePresenter.toggleUserCourse(action)
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