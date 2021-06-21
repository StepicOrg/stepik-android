package org.stepik.android.view.course.ui.delegates

import android.app.Activity
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.strikeThrough
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
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.analytic.batch.BuyCoursePressedAnalyticBatchEvent
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_continue.analytic.CourseContinuePressedEvent
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.domain.course_payments.model.PromoCode
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.user_courses.model.UserCourseAction
import org.stepik.android.presentation.wishlist.model.WishlistAction
import org.stepik.android.view.base.ui.extension.ColorExtensions
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.view.base.ui.extension.getAllQueryParameters
import java.util.TimeZone
import kotlin.math.abs

class CourseHeaderDelegate(
    private val courseActivity: Activity,
    private val analytic: Analytic,
    private val coursePresenter: CoursePresenter,
    private val discountButtonAppearanceSplitTest: DiscountButtonAppearanceSplitTest,
    private val displayPriceMapper: DisplayPriceMapper,
    private val courseViewSource: CourseViewSource,
    onSubmissionCountClicked: () -> Unit,
    isLocalSubmissionsEnabled: Boolean
) {
    companion object {
        private val CourseHeaderData.enrolledState: EnrollmentState.Enrolled?
            get() = stats.enrollmentState.safeCast<EnrollmentState.Enrolled>()
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
                    analytic.report(
                        CourseContinuePressedEvent(
                            headerData.course,
                            CourseContinueInteractionSource.COURSE_SCREEN,
                            courseViewSource
                        )
                    )
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
            analytic.report(BuyCoursePressedAnalyticBatchEvent(headerData.courseId))
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

            val (currencyCode, promoPrice, hasPromo) = when {
                courseHeaderData.promoCode != PromoCode.EMPTY ->
                    Triple(courseHeaderData.promoCode.currencyCode, courseHeaderData.promoCode.price, true)

                courseHeaderData.defaultPromoCode != DefaultPromoCode.EMPTY &&
                        (courseHeaderData.defaultPromoCode.defaultPromoCodeExpireDate == null || courseHeaderData.defaultPromoCode.defaultPromoCodeExpireDate.time > DateTimeHelper.nowUtc()) &&
                        courseHeaderData.course.currencyCode != null ->
                    Triple(courseHeaderData.course.currencyCode!!, courseHeaderData.defaultPromoCode.defaultPromoCodePrice, true)

                else ->
                    Triple("", "", false)
            }

            val courseDisplayPrice = courseHeaderData.course.displayPrice

            courseBuyInWebAction.text =
                if (courseDisplayPrice != null) {
                    if (hasPromo) {
                        getPurchaseButtonText(courseDisplayPrice, currencyCode, promoPrice)
                    } else {
                        getString(R.string.course_payments_purchase_in_web_with_price, courseDisplayPrice)
                    }
                } else {
                    getString(R.string.course_payments_purchase_in_web)
                }

            courseDefaultPromoInfo.text = courseHeaderData.defaultPromoCode.defaultPromoCodeExpireDate?.let {
                val formattedDate = DateTimeHelper.getPrintableDate(it, DateTimeHelper.DISPLAY_DAY_MONTH_PATTERN, TimeZone.getDefault())
                getString(R.string.course_promo_code_date, formattedDate)
            }

            courseDefaultPromoInfo.isVisible = (courseHeaderData.defaultPromoCode.defaultPromoCodeExpireDate?.time ?: -1L) > DateTimeHelper.nowUtc() &&
                    courseHeaderData.course.enrollment == 0L

            courseBuyInWebActionDiscountedNewPrice.text =
                getString(R.string.course_payments_purchase_in_web_with_price, displayPriceMapper.mapToDisplayPrice(currencyCode, promoPrice))

            courseBuyInWebActionDiscountedOldPrice.text =
                buildSpannedString {
                    strikeThrough {
                        append(courseHeaderData.course.displayPrice)
                    }
                }

            if (courseHeaderData.course.displayPrice != null && hasPromo) {
                when (discountButtonAppearanceSplitTest.currentGroup) {
                    DiscountButtonAppearanceSplitTest.Group.DiscountTransparent -> {
                        courseBuyInWebAction.isVisible = false
                        courseBuyInWebActionDiscounted.isVisible = true
                    }
                    DiscountButtonAppearanceSplitTest.Group.DiscountGreen -> {
                        courseBuyInWebAction.isVisible = true
                        courseBuyInWebActionDiscounted.isVisible = false
                        ViewCompat.setBackgroundTintList(courseBuyInWebAction, AppCompatResources.getColorStateList(courseActivity, R.color.color_overlay_green))
                    }
                    DiscountButtonAppearanceSplitTest.Group.DiscountPurple -> {
                        courseBuyInWebAction.isVisible = true
                        courseBuyInWebActionDiscounted.isVisible = false
                        ViewCompat.setBackgroundTintList(courseBuyInWebAction, AppCompatResources.getColorStateList(courseActivity, R.color.color_overlay_violet))
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

    private fun getPurchaseButtonText(originalDisplayPrice: String, currencyCode: String, promoPrice: String): SpannedString {
        val promoDisplayPrice = displayPriceMapper.mapToDisplayPrice(currencyCode, promoPrice)
        return buildSpannedString {
            append(courseActivity.getString(R.string.course_payments_purchase_in_web_with_price_promo))
            append(promoDisplayPrice)
            append(" ")
            scale(0.9f) {
                strikeThrough {
                    append(originalDisplayPrice)
                }
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

        menu.findItem(R.id.wishlist_course)
            ?.let { wishlistCourseMenuItem ->
                wishlistCourseMenuItem.isVisible = courseHeaderData != null
                wishlistCourseMenuItem.isEnabled = courseHeaderData?.isWishlistUpdating == false
                val (icon, title) =
                    if (courseHeaderData?.stats?.isWishlisted == true) {
                        ContextCompat.getDrawable(courseActivity, R.drawable.ic_wishlist_active) to courseActivity.getString(R.string.wishlist_add_action)
                    } else {
                        ContextCompat.getDrawable(courseActivity, R.drawable.ic_wishlist_inactive) to courseActivity.getString(R.string.wishlist_remove_action)
                    }
                wishlistCourseMenuItem.icon = icon
                wishlistCourseMenuItem.title = title
            }

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

            R.id.wishlist_course -> {
                courseHeaderData?.stats?.let {
                    val action =
                        if (it.isWishlisted) {
                            WishlistAction.REMOVE
                        } else {
                            WishlistAction.ADD
                        }
                    coursePresenter.toggleWishlist(action)
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