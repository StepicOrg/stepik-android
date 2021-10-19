package org.stepik.android.view.course_purchase.ui.dialog

import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
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
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import timber.log.Timber
import javax.inject.Inject

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

    private var coursePurchaseData: CoursePurchaseData by argument()

    private val coursePurchaseViewModel: CoursePurchaseViewModel by reduxViewModel(this) { viewModelFactory }
    private val coursePurchaseBinding: BottomSheetDialogCoursePurchaseBinding by viewBinding(BottomSheetDialogCoursePurchaseBinding::bind)

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
        val initialCoursePromoCodeInfo = coursePromoCodeResolver.resolvePromoCodeInfo(coursePurchaseData.deeplinkPromoCode, coursePurchaseData.defaultPromoCode, coursePurchaseData.course)
        coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.InitMessage(coursePurchaseData, initialCoursePromoCodeInfo))
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_course_purchase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        promoCodeViewDelegate = PromoCodeViewDelegate(coursePurchaseBinding, coursePurchaseViewModel, coursePurchaseData, displayPriceMapper, coursePromoCodeResolver)
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
    }

    override fun onAction(action: CoursePurchaseFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: CoursePurchaseFeature.State) {
        if (state is CoursePurchaseFeature.State.Content) {
            Timber.d("APPS - Promo code state: ${state.promoCodeState}")
            Timber.d("APPS - Wishlist state: ${state.wishlistState}")
            promoCodeViewDelegate.render(state.promoCodeState)
            wishlistViewDelegate.render(state.wishlistState)
        }
    }
}