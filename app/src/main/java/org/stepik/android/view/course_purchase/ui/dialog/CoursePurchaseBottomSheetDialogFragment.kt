package org.stepik.android.view.course_purchase.ui.dialog

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.databinding.BottomSheetDialogCoursePurchaseBinding
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.course_purchase.CoursePurchaseViewModel
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course.resolver.CoursePromoCodeResolver
import org.stepik.android.view.course_purchase.delegate.PromoCodeViewDelegate
import org.stepik.android.view.course_purchase.delegate.WishlistViewDelegate
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject
import androidx.annotation.StringRes
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.view.course_purchase.delegate.BuyActionViewDelegate
import ru.nobird.android.view.base.ui.extension.snackbar

class CoursePurchaseBottomSheetDialogFragment :
    BottomSheetDialogFragment(),
    ReduxView<CoursePurchaseFeature.State, CoursePurchaseFeature.Action.ViewAction> {
    companion object {
        const val TAG = "CoursePurchaseBottomSheetDialogFragment"

        fun newInstance(coursePurchaseData: CoursePurchaseData): DialogFragment =
            CoursePurchaseBottomSheetDialogFragment().apply {
                this.coursePurchaseData = coursePurchaseData
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    @Inject
    internal lateinit var coursePromoCodeResolver: CoursePromoCodeResolver

    @Inject
    internal lateinit var billingClient: BillingClient

    private var coursePurchaseData: CoursePurchaseData by argument()

    private val coursePurchaseViewModel: CoursePurchaseViewModel by reduxViewModel(this) { viewModelFactory }
    private val coursePurchaseBinding: BottomSheetDialogCoursePurchaseBinding by viewBinding(BottomSheetDialogCoursePurchaseBinding::bind)

    private lateinit var buyActionViewDelegate: BuyActionViewDelegate
    private lateinit var promoCodeViewDelegate: PromoCodeViewDelegate
    private lateinit var wishlistViewDelegate: WishlistViewDelegate

    private fun injectComponent() {
        App
            .component()
            .coursePurchaseComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)
        coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.InitMessage(coursePurchaseData))
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_course_purchase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buyActionViewDelegate = BuyActionViewDelegate(coursePurchaseBinding, coursePurchaseData, displayPriceMapper) { coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.LaunchPurchaseFlow) }
        promoCodeViewDelegate = PromoCodeViewDelegate(coursePurchaseBinding, coursePurchaseViewModel)
        wishlistViewDelegate = WishlistViewDelegate(coursePurchaseBinding.coursePurchaseWishlistAction)
        coursePurchaseBinding.coursePurchaseWishlistAction.setOnClickListener {
            coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.WishlistAddMessage)
        }

        coursePurchaseBinding.coursePurchaseCourseTitle.text = coursePurchaseData.course.title.orEmpty()
        Glide
            .with(requireContext())
            .asBitmap()
            .load(coursePurchaseData.course.cover)
            .placeholder(R.drawable.general_placeholder)
            .fitCenter()
            .into(coursePurchaseBinding.coursePurchaseCourseIcon)

        val userAgreementLinkSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val userAgreementUrl = getString(R.string.course_purchase_commission_url)

                InAppWebViewDialogFragment
                    .newInstance(getString(R.string.course_purchase_commission_web_view_title), userAgreementUrl, isProvideAuth = false)
                    .showIfNotExists(childFragmentManager, InAppWebViewDialogFragment.TAG)
            }
        }
        coursePurchaseBinding.coursePurchaseCommissionNotice.text = buildSpannedString {
            append(getString(R.string.course_purchase_commission_information_part_1))
            inSpans(userAgreementLinkSpan) {
                append(getString(R.string.course_purchase_commission_information_part_2))
            }
            append(getString(R.string.full_stop))
        }
        coursePurchaseBinding.coursePurchaseCommissionNotice.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onAction(action: CoursePurchaseFeature.Action.ViewAction) {
        when (action) {
            is CoursePurchaseFeature.Action.ViewAction.LaunchPurchaseFlowBilling -> {
                val billingFlowParams = BillingFlowParams
                    .newBuilder()
                    .setObfuscatedAccountId(action.obfuscatedParams.obfuscatedAccountId)
                    .setObfuscatedProfileId(action.obfuscatedParams.obfuscatedProfileId)
                    .setSkuDetails(action.skuDetails)
                    .build()

                billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
            }
            is CoursePurchaseFeature.Action.ViewAction.Error -> {
                @StringRes
                val errorMessage =
                    when (action.error) {
                        EnrollmentError.NO_CONNECTION ->
                            R.string.course_error_enroll

                        EnrollmentError.FORBIDDEN ->
                            R.string.join_course_web_exception

                        EnrollmentError.UNAUTHORIZED ->
                            R.string.unauthorization_detail

                        EnrollmentError.SERVER_ERROR ->
                            R.string.course_purchase_server_error

                        EnrollmentError.BILLING_ERROR ->
                            R.string.course_purchase_billing_error

                        EnrollmentError.BILLING_CANCELLED ->
                            R.string.course_purchase_billing_cancelled

                        EnrollmentError.BILLING_NOT_AVAILABLE ->
                            R.string.course_purchase_billing_not_available

                        EnrollmentError.COURSE_ALREADY_OWNED ->
                            R.string.course_purchase_already_owned

                        EnrollmentError.BILLING_NO_PURCHASES_TO_RESTORE ->
                            R.string.course_purchase_billing_no_purchases_to_restore
                    }
                coursePurchaseBinding.coursePurchaseCoordinator.snackbar(messageRes = errorMessage)
            }
        }
    }

    override fun render(state: CoursePurchaseFeature.State) {
        if (state is CoursePurchaseFeature.State.Content) {
            isCancelable = state.paymentState is CoursePurchaseFeature.PaymentState.Idle || state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure || state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess
            buyActionViewDelegate.render(state)

            if (state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure || state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess) {
                promoCodeViewDelegate.setViewVisibility(isVisible = false)
                wishlistViewDelegate.setViewVisibility(isVisible = false)
            } else {
                promoCodeViewDelegate.render(state.promoCodeState)
                wishlistViewDelegate.setViewVisibility(isVisible = true)
                wishlistViewDelegate.render(state.wishlistState)
            }

            val buyActionButtonColor = getBuyActionColor(state.promoCodeState)
            val (strokeColor, textColor) = getWishlistActionColor(state)

            coursePurchaseBinding.coursePurchaseBuyAction.isEnabled = state.paymentState is CoursePurchaseFeature.PaymentState.Idle || state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure || state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess
            coursePurchaseBinding.coursePurchaseBuyAction.setBackgroundColor(ContextCompat.getColor(requireContext(), buyActionButtonColor))
            coursePurchaseBinding.coursePurchaseWishlistAction.strokeColor = AppCompatResources.getColorStateList(requireContext(), strokeColor)
            coursePurchaseBinding.coursePurchaseWishlistAction.setTextColor(AppCompatResources.getColorStateList(requireContext(), textColor))
        }
    }

    private fun getBuyActionColor(promoCodeState: CoursePurchaseFeature.PromoCodeState): Int =
        if (promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid) {
            R.color.color_overlay_green
        } else {
            R.color.color_overlay_violet
        }

    private fun getWishlistActionColor(state: CoursePurchaseFeature.State.Content): Pair<Int, Int> =
        if (state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid) {
            if (state.wishlistState is CoursePurchaseFeature.WishlistState.Wishlisted) {
                R.color.color_overlay_green_alpha_12 to R.color.color_overlay_green
            } else {
                R.color.color_overlay_green to R.color.color_overlay_green
            }
        } else {
            if (state.wishlistState is CoursePurchaseFeature.WishlistState.Wishlisted) {
                R.color.color_overlay_violet_alpha_12 to R.color.color_overlay_violet
            } else {
                R.color.color_overlay_violet to R.color.color_overlay_violet
            }
        }

    interface Callback {
        fun purchaseCourse(primarySku: LightSku, promoCodeSku: PromoCodeSku)
    }
}