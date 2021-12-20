package org.stepik.android.view.course_purchase.ui.dialog

import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.course_purchase.CoursePurchaseViewModel
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course.resolver.CoursePromoCodeResolver
import org.stepik.android.view.course_purchase.delegate.PromoCodeViewDelegate
import org.stepik.android.view.course_purchase.delegate.WishlistViewDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.util.DeviceInfoUtil
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.defaultLocale
import org.stepik.android.domain.course_purchase.analytic.RestoreCoursePurchaseSource
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.view.course_purchase.delegate.BuyActionViewDelegate
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.base.ui.extension.snackbar
import timber.log.Timber

class CoursePurchaseBottomSheetDialogFragment :
    BottomSheetDialogFragment(),
    ReduxView<CoursePurchaseFeature.State, CoursePurchaseFeature.Action.ViewAction> {
    companion object {
        const val TAG = "CoursePurchaseBottomSheetDialogFragment"

        fun newInstance(coursePurchaseData: CoursePurchaseData, coursePurchaseSource: String, isNeedRestoreMessage: Boolean): DialogFragment =
            CoursePurchaseBottomSheetDialogFragment().apply {
                this.coursePurchaseData = coursePurchaseData
                this.coursePurchaseSource = coursePurchaseSource
                this.isNeedRestoreMessage = isNeedRestoreMessage
            }

        private const val RUSSIAN_LANGUAGE_CODE = "ru"
        private const val BELARUSIAN_LANGUAGE_CODE = "be"
    }

    @Inject
    internal lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    @Inject
    internal lateinit var coursePromoCodeResolver: CoursePromoCodeResolver

    @Inject
    internal lateinit var billingClient: BillingClient

    private var coursePurchaseData: CoursePurchaseData by argument()
    private var coursePurchaseSource: String by argument()
    private var isNeedRestoreMessage: Boolean by argument()

    private val coursePurchaseViewModel: CoursePurchaseViewModel by reduxViewModel(this) { viewModelFactory }
    private val coursePurchaseBinding: BottomSheetDialogCoursePurchaseBinding by viewBinding(BottomSheetDialogCoursePurchaseBinding::bind)

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

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
        coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.InitMessage(coursePurchaseData, coursePurchaseSource))
        if (isNeedRestoreMessage) {
            coursePurchaseViewModel.onNewMessage(
                CoursePurchaseFeature.Message.LaunchRestorePurchaseFlow(RestoreCoursePurchaseSource.COURSE_SCREEN)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_course_purchase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buyActionViewDelegate = BuyActionViewDelegate(coursePurchaseBinding, coursePurchaseData, displayPriceMapper,
            launchPurchaseFlowAction =  { coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.LaunchPurchaseFlow) },
            launchStartStudying = { coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.StartLearningMessage) },
            launchRestoreAction = {
                coursePurchaseViewModel.onNewMessage(
                    CoursePurchaseFeature.Message.LaunchRestorePurchaseFlow(RestoreCoursePurchaseSource.BUY_COURSE_DIALOG)
                )
            }
        )
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

        val supportSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                coursePurchaseViewModel.onNewMessage(
                    CoursePurchaseFeature.Message.SetupFeedback(
                        getString(R.string.feedback_subject),
                        DeviceInfoUtil.getInfosAboutDevice(requireContext(), "\n")
                    )
                )
            }
        }

        coursePurchaseBinding.coursePurchasePaymentFeedback.text = buildSpannedString {
            append(getString(R.string.course_purchase_payment_failure_body_part_1))
            inSpans(supportSpan) {
                append(getString(R.string.course_purchase_payment_failure_body_part_2))
            }
            append(getString(R.string.course_purchase_payment_failure_body_part_3))
        }
        coursePurchaseBinding.coursePurchasePaymentFeedback.movementMethod = LinkMovementMethod.getInstance()

        coursePurchaseBinding.coursePurchaseCommissionNotice.text = buildSpannedString {
            append(resolveCommissionSpannedText())
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

            is CoursePurchaseFeature.Action.ViewAction.ShowLoading ->
                ProgressHelper.activate(progressDialogFragment, childFragmentManager, LoadingProgressDialogFragment.TAG)

            is CoursePurchaseFeature.Action.ViewAction.ShowConsumeSuccess -> {
                ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)
            }

            is CoursePurchaseFeature.Action.ViewAction.ShowConsumeFailure -> {
                Timber.d("APPS Action: $action")
                ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)
            }

            is CoursePurchaseFeature.Action.ViewAction.StartStudyAction -> {
                (activity as? Callback ?: parentFragment as? Callback)?.continueLearning()
                dismiss()
            }

            is CoursePurchaseFeature.Action.ViewAction.ShowContactSupport -> {
                screenManager.openTextFeedBack(requireContext(), action.supportEmailData)
            }
        }
    }

    override fun render(state: CoursePurchaseFeature.State) {
        if (state is CoursePurchaseFeature.State.Content) {
            isCancelable = state.paymentState is CoursePurchaseFeature.PaymentState.Idle ||
                state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure ||
                state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess

            buyActionViewDelegate.render(state)

            if (state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure ||
                state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess
            ) {
                promoCodeViewDelegate.setViewVisibility(isVisible = false)
                wishlistViewDelegate.setViewVisibility(isVisible = false)
            } else {
                promoCodeViewDelegate.render(state.promoCodeState)
                wishlistViewDelegate.setViewVisibility(isVisible = true)
                wishlistViewDelegate.render(state)
            }

            (state.paymentState is CoursePurchaseFeature.PaymentState.Idle ||
                state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure ||
                state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess
            ).let { mustEnable ->
                isCancelable = mustEnable
                coursePurchaseBinding.coursePurchaseBuyActionGreen.isEnabled = mustEnable
                coursePurchaseBinding.coursePurchaseBuyActionViolet.isEnabled = mustEnable
            }
        }
    }

    private fun resolveCommissionSpannedText(): Spanned {
        val userAgreementConfigKey =
            when (resources.configuration.defaultLocale.language) {
                RUSSIAN_LANGUAGE_CODE ->
                    RemoteConfig.PURCHASE_FLOW_DISCLAIMER_RU
                BELARUSIAN_LANGUAGE_CODE ->
                    RemoteConfig.PURCHASE_FLOW_DISCLAIMER_BE
                else ->
                    RemoteConfig.PURCHASE_FLOW_DISCLAIMER_EN
            }

        val userAgreementSpannedText = HtmlCompat.fromHtml(firebaseRemoteConfig[userAgreementConfigKey].asString(), HtmlCompat.FROM_HTML_MODE_COMPACT).toSpannable()

        for (span in userAgreementSpannedText.getSpans<URLSpan>()) {
            val start = userAgreementSpannedText.getSpanStart(span)
            val end = userAgreementSpannedText.getSpanEnd(span)
            val flags = userAgreementSpannedText.getSpanFlags(span)

            val userAgreementLinkSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    InAppWebViewDialogFragment
                        .newInstance(getString(R.string.course_purchase_commission_web_view_title), span.url, isProvideAuth = false)
                        .showIfNotExists(childFragmentManager, InAppWebViewDialogFragment.TAG)
                }
            }

            userAgreementSpannedText.removeSpan(span)
            userAgreementSpannedText.setSpan(userAgreementLinkSpan, start, end, flags)
        }

        return userAgreementSpannedText
    }

    interface Callback {
        fun continueLearning()
    }
}