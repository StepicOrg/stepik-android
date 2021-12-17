package org.stepik.android.view.lesson_demo.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.android.synthetic.main.bottom_sheet_dialog_lesson_demo_complete.*
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.ScreenManager
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.domain.course_purchase.analytic.CoursePurchaseSource
import org.stepik.android.domain.course_purchase.model.CoursePurchaseFlow
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature
import org.stepik.android.presentation.lesson_demo.LessonDemoViewModel
import org.stepik.android.presentation.wishlist.WishlistOperationFeature
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course.resolver.CoursePromoCodeResolver
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course_purchase.delegate.WishlistViewDelegate
import org.stepik.android.view.course_purchase.ui.dialog.CoursePurchaseBottomSheetDialogFragment
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class LessonDemoCompleteBottomSheetDialogFragment :
    BottomSheetDialogFragment(),
    CoursePurchaseBottomSheetDialogFragment.Callback,
    ReduxView<LessonDemoFeature.State, LessonDemoFeature.Action.ViewAction> {
    companion object {
        const val TAG = "LessonDemoCompleteBottomSheetDialog"

        fun newInstance(course: Course): DialogFragment =
            LessonDemoCompleteBottomSheetDialogFragment().apply {
                this.course = course
            }
    }

    private var course: Course by argument()

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    @Inject
    internal lateinit var coursePromoCodeResolver: CoursePromoCodeResolver

    @Inject
    internal lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    private val lessonDemoViewModel: LessonDemoViewModel by reduxViewModel(this) { viewModelFactory }
    private val viewStateDelegate = ViewStateDelegate<LessonDemoFeature.LessonDemoState>()
    private lateinit var wishlistViewDelegate: WishlistViewDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager()
            .courseComponent(course.id)
            .lessonDemoPresentationComponentBuilder()
            .build()
            .inject(this)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)
        lessonDemoViewModel.onNewMessage(LessonDemoFeature.Message.InitMessage(course))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_lesson_demo_complete, container, false)

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewStateDelegate()
        wishlistViewDelegate = WishlistViewDelegate(demoWishlistAction)
        demoCompleteTitle.text = getString(R.string.demo_complete_title, course.title)
        demoCompleteAction.setOnClickListener {
            lessonDemoViewModel.onNewMessage(LessonDemoFeature.Message.BuyActionMessage)
        }
        demoWishlistAction.setOnClickListener {
            lessonDemoViewModel.onNewMessage(
                LessonDemoFeature.Message.WishlistMessage(
                    WishlistOperationFeature.Message.WishlistAddMessage(course, CourseViewSource.LessonDemoDialog)
                )
            )
        }
        tryAgain.setOnClickListener { lessonDemoViewModel.onNewMessage(LessonDemoFeature.Message.InitMessage(course, forceUpdate = true)) }
    }

    override fun onAction(action: LessonDemoFeature.Action.ViewAction) {
        if (action is LessonDemoFeature.Action.ViewAction.BuyAction) {
            val currentFlow = CoursePurchaseFlow.valueOfWithFallback(
                firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID]
                    .asString()
                    .uppercase()
            )

            val isInAppActive =
                currentFlow.isInAppActive() || RemoteConfig.PURCHASE_FLOW_ANDROID_TESTING_FLAG

            if (isInAppActive && action.coursePurchaseData != null) {
                CoursePurchaseBottomSheetDialogFragment
                    .newInstance(action.coursePurchaseData, CoursePurchaseSource.DEMO_LESSON_DIALOG_SOURCE, isNeedRestoreMessage = false)
                    .showIfNotExists(childFragmentManager, CoursePurchaseBottomSheetDialogFragment.TAG)
            } else {
                screenManager.showCoursePurchaseFromLessonDemoDialog(
                    requireContext(),
                    course.id,
                    CourseViewSource.LessonDemoDialog,
                    CourseScreenTab.INFO,
                    action.deeplinkPromoCode
                )
            }
        }
    }

    override fun render(state: LessonDemoFeature.State) {
        if (state is LessonDemoFeature.State.Content) {
            viewStateDelegate.switchState(state.lessonDemoState)
            if (state.lessonDemoState is LessonDemoFeature.LessonDemoState.Content) {
                if (state.lessonDemoState.coursePurchaseData != null) {
                    setupIAP(state.lessonDemoState.coursePurchaseData)
                } else {
                    setupWeb(state.lessonDemoState.deeplinkPromoCode)
                }
            }

            wishlistViewDelegate.render(state.wishlistOperationState, mustEnable = true)
        }
    }

    override fun onDestroy() {
        App.componentManager().releaseCourseComponent(course.id)
        super.onDestroy()
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<LessonDemoFeature.LessonDemoState.Idle>()
        viewStateDelegate.addState<LessonDemoFeature.LessonDemoState.Loading>(demoCompleteProgressbar)
        viewStateDelegate.addState<LessonDemoFeature.LessonDemoState.Error>(demoCompleteNetworkError)
        viewStateDelegate.addState<LessonDemoFeature.LessonDemoState.Unavailable>(demoCompleteContent, demoCompleteTitle, demoPurchaseUnavailable, demoWishlistAction)
        viewStateDelegate.addState<LessonDemoFeature.LessonDemoState.Content>(demoCompleteContent, demoCompleteTitle, demoCompleteInfo, demoCompleteDivider, demoCompleteAction, demoWishlistAction)
    }

    private fun setupWeb(deeplinkPromoCode: DeeplinkPromoCode) {
        val courseDisplayPrice = course.displayPrice
        val (_, currencyCode, promoPrice, hasPromo) = coursePromoCodeResolver.resolvePromoCodeInfo(
            deeplinkPromoCode,
            DefaultPromoCode(
                course.defaultPromoCodeName ?: "",
                course.defaultPromoCodePrice ?: "",
                course.defaultPromoCodeDiscount ?: "",
                course.defaultPromoCodeExpireDate
            ),
            course
        )
        demoCompleteAction.text =
            if (courseDisplayPrice != null) {
                if (hasPromo) {
                    displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(courseDisplayPrice, promoPrice, currencyCode)
                } else {
                    getString(R.string.course_payments_purchase_in_web_with_price, courseDisplayPrice)
                }
            } else {
                getString(R.string.course_payments_purchase_in_web)
            }
    }

    private fun setupIAP(coursePurchaseData: CoursePurchaseData) {
        val courseDisplayPrice = coursePurchaseData.course.displayPrice
        demoCompleteAction.text =
            if (courseDisplayPrice != null) {
                if (coursePurchaseData.promoCodeSku.lightSku != null) {
                    displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(coursePurchaseData.primarySku.price, coursePurchaseData.promoCodeSku.lightSku.price)
                } else {
                    getString(R.string.course_payments_purchase_in_web_with_price, coursePurchaseData.primarySku.price)
                }
            } else {
                getString(R.string.course_payments_purchase_in_web)
            }
    }

    override fun continueLearning() {
        screenManager.showCourseAfterPurchase(requireContext(), course, CourseViewSource.LessonDemoDialog, CourseScreenTab.INFO)
    }
}