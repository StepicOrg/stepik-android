package org.stepik.android.view.course.ui.delegates

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.strikeThrough
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
import org.stepic.droid.util.DateTimeHelper.getPrintableOfIsoDate
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.domain.course.analytic.BuyCoursePressedEvent
import org.stepik.android.domain.course.analytic.CourseJoinedEvent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.analytic.batch.BuyCoursePressedAnalyticBatchEvent
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_continue.analytic.CourseContinuePressedEvent
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.course.resolver.CoursePurchaseDataResolver
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.presentation.user_courses.model.UserCourseAction
import org.stepik.android.presentation.wishlist.model.WishlistAction
import org.stepik.android.view.base.ui.extension.ColorExtensions
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course.resolver.CoursePromoCodeResolver
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.core.model.safeCast
import ru.nobird.android.view.base.ui.extension.getAllQueryParameters
import ru.nobird.android.view.base.ui.extension.getDrawableCompat
import java.util.TimeZone
import kotlin.math.abs

class CourseHeaderDelegate
@AssistedInject
constructor(
    @Assisted private val courseActivity: Activity,
    private val analytic: Analytic,
    @Assisted private val coursePresenter: CoursePresenter,
    private val discountButtonAppearanceSplitTest: DiscountButtonAppearanceSplitTest,
    private val displayPriceMapper: DisplayPriceMapper,
    private val coursePromoCodeResolver: CoursePromoCodeResolver,
    private val coursePurchaseDataResolver: CoursePurchaseDataResolver,
    @Assisted private val courseViewSource: CourseViewSource,
    @Assisted("isAuthorized")
    private val isAuthorized: Boolean,
    @Assisted("mustShowCourseRevenue")
    private val mustShowCourseRevenue: Boolean,
    @Assisted("showCourseRevenueAction")
    private val showCourseRevenueAction: () -> Unit,
    @Assisted("onSubmissionCountClicked")
    onSubmissionCountClicked: () -> Unit,
    @Assisted("isLocalSubmissionsEnabled")
    isLocalSubmissionsEnabled: Boolean,
    @Assisted("showCourseSearchAction")
    private val showSearchCourseAction: () -> Unit,
    @Assisted private val coursePurchaseFlowAction: (CoursePurchaseData, Boolean) -> Unit
) {
    companion object {
        private val CourseHeaderData.enrolledState: EnrollmentState.Enrolled?
            get() = stats.enrollmentState.safeCast<EnrollmentState.Enrolled>()

        private const val EVALUATION_FRAME_DURATION_MS = 250
    }

    var courseHeaderData: CourseHeaderData? = null
        set(value) {
            field = value
            value?.let(::setCourseData)
        }

    private var courseBenefitsMenuItem: MenuItem? = null
    private var courseSearchMenuItem: MenuItem? = null
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
                    analytic.report(
                        CourseJoinedEvent(
                            CourseJoinedEvent.SOURCE_PREVIEW,
                            headerData.course,
                            headerData.course.isInWishlist
                        )
                    )
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

            courseWishlistAction.setOnClickListener {
                courseHeaderData?.let {
                    val action =
                        if (it.course.isInWishlist) {
                            WishlistAction.REMOVE
                        } else {
                            WishlistAction.ADD
                        }
                    coursePresenter.toggleWishlist(action)
                }
            }

            courseBuyInWebAction.setOnClickListener {
                courseHeaderData?.let(::setupBuyAction)
            }

            courseBuyInWebActionDiscounted.setOnClickListener {
                courseHeaderData?.let(::setupBuyAction)
            }

            courseTryFree.setOnClickListener {
                val course = courseHeaderData
                    ?.course
                    ?: return@setOnClickListener

                coursePresenter.tryLessonFree(course.previewLesson, course.previewUnit)
            }
        }
    }

    private fun buyInWebAction() {
        val queryParams = courseActivity
            .intent
            ?.data
            ?.getAllQueryParameters()

        coursePresenter.openCoursePurchaseInWeb(queryParams)
    }

    private fun initViewStateDelegate() {
        with(courseActivity) {
            viewStateDelegate.addState<EnrollmentState.Enrolled>(courseContinueAction)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledFree>(courseEnrollAction)
            viewStateDelegate.addState<EnrollmentState.Pending>(courseEnrollmentProgress)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledUnavailableIAP>(courseWishlistAction, coursePurchaseFeedback)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledEnded>(courseWishlistAction, coursePurchaseFeedback)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledCantBeBought>(courseWishlistAction, coursePurchaseFeedback)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledWeb>(purchaseContainer)
            viewStateDelegate.addState<EnrollmentState.NotEnrolledMobileTier>(purchaseContainer)
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

            setupWishlistAction(courseHeaderData)
            /**
             * Purchase setup section
             */

            if (courseHeaderData.stats.enrollmentState is EnrollmentState.NotEnrolledMobileTier) {
                setupIAP(courseHeaderData)
            } else {
                setupWeb(courseHeaderData)
            }

            courseDefaultPromoInfo.text = courseHeaderData.defaultPromoCode.defaultPromoCodeExpireDate?.let {
                val formattedDate = DateTimeHelper.getPrintableDate(it, DateTimeHelper.DISPLAY_DAY_MONTH_PATTERN, TimeZone.getDefault())
                getString(R.string.course_promo_code_date, formattedDate)
            }

            courseDefaultPromoInfo.isVisible = (courseHeaderData.defaultPromoCode.defaultPromoCodeExpireDate?.time ?: -1L) > DateTimeHelper.nowUtc() &&
                courseHeaderData.course.enrollment == 0L &&
                (courseHeaderData.deeplinkPromoCode == DeeplinkPromoCode.EMPTY || courseHeaderData.deeplinkPromoCode.name == courseHeaderData.defaultPromoCode.defaultPromoCodeName)

            with(courseHeaderData.stats.enrollmentState) {
                viewStateDelegate.switchState(this)

                dropCourseMenuItem?.isVisible = this is EnrollmentState.Enrolled
                restorePurchaseCourseMenuItem?.isVisible = this is EnrollmentState.NotEnrolledMobileTier
            }

            courseTryFree.isVisible = courseHeaderData.course.previewLesson != 0L &&
                    courseHeaderData.course.enrollment == 0L &&
                    courseHeaderData.course.isPaid &&
                    (courseHeaderData.stats.enrollmentState is EnrollmentState.NotEnrolledMobileTier ||
                        courseHeaderData.stats.enrollmentState is EnrollmentState.NotEnrolledWeb ||
                        courseHeaderData.stats.enrollmentState is EnrollmentState.NotEnrolledUnavailableIAP)

            shareCourseMenuItem?.isVisible = true
            setupPurchaseFeedback(courseHeaderData)
        }

    private fun setupPurchaseFeedback(courseHeaderData: CourseHeaderData) {
        with(courseActivity) {
            coursePurchaseFeedback.text =
                when (courseHeaderData.stats.enrollmentState) {
                    is EnrollmentState.NotEnrolledUnavailableIAP ->
                        getString(R.string.course_purchase_unavailable)
                    is EnrollmentState.NotEnrolledEnded ->
                        if (courseHeaderData.course.endDate != null) {
                            getString(
                                R.string.course_payments_not_available_ended,
                                getPrintableOfIsoDate(courseHeaderData.course.endDate, DateTimeHelper.DISPLAY_DAY_MONTH_YEAR_GENITIVE_PATTERN, TimeZone.getDefault())
                            )
                        } else {
                            getString(R.string.course_payments_not_available)
                        }
                    is EnrollmentState.NotEnrolledCantBeBought ->
                        getString(R.string.course_payments_cant_be_bought)
                    else -> ""
                }
        }
    }

    private fun setupIAP(courseHeaderData: CourseHeaderData) {
        with(courseActivity) {
            val notEnrolledMobileTierState = courseHeaderData.stats.enrollmentState as EnrollmentState.NotEnrolledMobileTier
            val promoCodeSku = when {
                courseHeaderData.deeplinkPromoCodeSku != PromoCodeSku.EMPTY ->
                    courseHeaderData.deeplinkPromoCodeSku

                notEnrolledMobileTierState.promoLightSku != null -> {
                    PromoCodeSku(courseHeaderData.course.defaultPromoCodeName.orEmpty(), notEnrolledMobileTierState.promoLightSku)
                }

                else ->
                    PromoCodeSku.EMPTY
            }

            courseBuyInWebAction.text =
                if (courseHeaderData.course.displayPrice != null) {
                    if (promoCodeSku.lightSku != null) {
                        displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(notEnrolledMobileTierState.standardLightSku.price, promoCodeSku.lightSku.price)
                    } else {
                        getString(R.string.course_payments_purchase_in_web_with_price, notEnrolledMobileTierState.standardLightSku.price)
                    }
                } else {
                    getString(R.string.course_payments_purchase_in_web)
                }

            courseBuyInWebActionDiscountedNewPrice.text =
                    getString(R.string.course_payments_purchase_in_web_with_price, promoCodeSku.lightSku?.price)

            courseBuyInWebActionDiscountedOldPrice.text =
                    buildSpannedString {
                        strikeThrough {
                            append(notEnrolledMobileTierState.standardLightSku.price)
                        }
                    }

            setupDiscountButtons(hasDiscount = promoCodeSku.lightSku != null)
        }
    }

    private fun setupWeb(courseHeaderData: CourseHeaderData) {
        with(courseActivity) {
            val (_, currencyCode, promoPrice, hasPromo) = coursePromoCodeResolver.resolvePromoCodeInfo(
                courseHeaderData.deeplinkPromoCode,
                courseHeaderData.defaultPromoCode,
                courseHeaderData.course
            )

            val courseDisplayPrice = courseHeaderData.course.displayPrice

            courseBuyInWebAction.text =
                if (courseDisplayPrice != null) {
                    if (hasPromo) {
                        displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(
                            courseDisplayPrice,
                            promoPrice,
                            currencyCode
                        )
                    } else {
                        getString(
                            R.string.course_payments_purchase_in_web_with_price,
                            courseDisplayPrice
                        )
                    }
                } else {
                    getString(R.string.course_payments_purchase_in_web)
                }

            courseBuyInWebActionDiscountedNewPrice.text =
                getString(R.string.course_payments_purchase_in_web_with_price, displayPriceMapper.mapToDisplayPriceWithCurrency(currencyCode, promoPrice))

            courseBuyInWebActionDiscountedOldPrice.text =
                buildSpannedString {
                    strikeThrough {
                        append(courseHeaderData.course.displayPrice)
                    }
                }

            setupDiscountButtons(hasDiscount = courseHeaderData.course.displayPrice != null && hasPromo)
        }
    }

    private fun setupBuyAction(courseHeaderData: CourseHeaderData) {
        coursePresenter.schedulePurchaseReminder()
        analytic.report(BuyCoursePressedEvent(courseHeaderData.course, BuyCoursePressedEvent.COURSE_SCREEN, courseHeaderData.course.isInWishlist))
        analytic.report(BuyCoursePressedAnalyticBatchEvent(courseHeaderData.courseId))
        val coursePurchaseData = coursePurchaseDataResolver.resolveCoursePurchaseData(courseHeaderData)
        if (coursePurchaseData != null) {
            coursePurchaseFlowAction(coursePurchaseData, false)
        } else {
            buyInWebAction()
        }
    }

    private fun setupDiscountButtons(hasDiscount: Boolean) {
        with(courseActivity) {
            if (hasDiscount) {
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
        }
    }

    private fun setupWishlistAction(courseHeaderData: CourseHeaderData) {
        with(courseActivity) {
            courseWishlistAction.isEnabled = !courseHeaderData.course.isInWishlist && !courseHeaderData.isWishlistUpdating

            val wishlistText = if (courseHeaderData.isWishlistUpdating) {
                if (courseHeaderData.course.isInWishlist) {
                    getString(R.string.course_purchase_wishlist_removing)
                } else {
                    getString(R.string.course_purchase_wishlist_adding)
                }
            } else {
                if (courseHeaderData.course.isInWishlist) {
                    getString(R.string.course_purchase_wishlist_added)
                } else {
                    getString(R.string.course_purchase_wishlist_add)
                }
            }
            courseWishlistAction.text = wishlistText

            if (courseHeaderData.isWishlistUpdating) {
                val evaluationDrawable = AnimationDrawable()
                evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), EVALUATION_FRAME_DURATION_MS)
                evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), EVALUATION_FRAME_DURATION_MS)
                evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), EVALUATION_FRAME_DURATION_MS)
                evaluationDrawable.isOneShot = false

                courseWishlistAction.icon = evaluationDrawable
                evaluationDrawable.start()
            } else {
                courseWishlistAction.icon = null
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

        courseBenefitsMenuItem = menu.findItem(R.id.course_benefits)
        courseBenefitsMenuItem?.isVisible = mustShowCourseRevenue

        courseSearchMenuItem = menu.findItem(R.id.course_search)
        courseSearchMenuItem?.isVisible = courseHeaderData?.stats?.enrollmentState is EnrollmentState.Enrolled

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
                wishlistCourseMenuItem.isVisible = courseHeaderData != null && courseHeaderData?.course?.enrollment == 0L && isAuthorized
                wishlistCourseMenuItem.isEnabled = courseHeaderData?.isWishlistUpdating == false
                val (icon, title) =
                    if (courseHeaderData?.course?.isInWishlist == true) {
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
        restorePurchaseCourseMenuItem?.isVisible = courseHeaderData?.stats?.enrollmentState is EnrollmentState.NotEnrolledMobileTier
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.course_benefits -> {
                showCourseRevenueAction()
                true
            }
            R.id.course_search -> {
                showSearchCourseAction()
                true
            }
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
                courseHeaderData?.course?.let {
                    val action =
                        if (it.isInWishlist) {
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
                val coursePurchaseData = courseHeaderData?.let(coursePurchaseDataResolver::resolveCoursePurchaseData)
                if (coursePurchaseData != null) {
                    coursePurchaseFlowAction(coursePurchaseData, true)
                }
                true
            }
            else ->
                false
        }
}