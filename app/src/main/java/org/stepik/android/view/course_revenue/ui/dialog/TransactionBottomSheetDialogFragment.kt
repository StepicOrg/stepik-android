package org.stepik.android.view.course_revenue.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_transaction.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.model.user.User
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import ru.nobird.android.view.base.ui.extension.argument
import java.util.TimeZone
import javax.inject.Inject

class TransactionBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "TransactionBottomSheetDialog"

        private const val ARG_USER = "user"
        private const val ARG_COURSE_TITLE = "course_title"

        private const val PERCENTAGE_SUFFIX = ".00"

        fun newInstance(courseBenefit: CourseBenefit, courseBeneficiary: CourseBeneficiary, user: User?, courseTitle: String?): DialogFragment =
            TransactionBottomSheetDialogFragment()
                .apply {
                    this.courseBenefit = courseBenefit
                    this.courseBeneficiary = courseBeneficiary
                    this.arguments?.putParcelable(ARG_USER, user)
                    this.arguments?.putString(ARG_COURSE_TITLE, courseTitle)
                }
    }

    private var courseBenefit: CourseBenefit by argument()
    private var courseBeneficiary: CourseBeneficiary by argument()
    private var user: User? = null
    private var courseTitle: String? = null

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)

        user = arguments?.getParcelable(ARG_USER)
        courseTitle = arguments?.getString(ARG_COURSE_TITLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_transaction, container, false)

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionTitle.text =
            if (courseBenefit.status == CourseBenefit.Status.DEBITED) {
                getString(R.string.transaction_title_purchase)
            } else {
                getString(R.string.transaction_title_refund)
            }
        transactionDateValue.text = DateTimeHelper.getPrintableDate(courseBenefit.time, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())

        transactionCourseValue.text = courseTitle.orEmpty()
        transactionCourseTitle.isVisible = courseTitle != null
        transactionCourseValue.isVisible = courseTitle != null

        transactionBuyerValue.text = user?.fullName.orEmpty()
        transactionBuyerTitle.isVisible = user != null
        transactionBuyerValue.isVisible = user != null
        buyerOverlayView.setOnClickListener { user?.let { screenManager.openProfile(requireContext(), it.id) } }

        transactionPaymentValue.text = displayPriceMapper.mapToDisplayPrice(courseBenefit.currencyCode, courseBenefit.paymentAmount)

        transactionPromoCodeValue.text = courseBenefit.promoCode.orEmpty()
        transactionPromoCodeTitle.isVisible = courseBenefit.promoCode != null
        transactionPromoCodeValue.isVisible = courseBenefit.promoCode != null

        transactionChannelTitle.isVisible = courseBenefit.status == CourseBenefit.Status.DEBITED
        transactionChannelValue.isVisible = courseBenefit.status == CourseBenefit.Status.DEBITED
        transactionChannelValue.text =
            when {
                courseBenefit.isZLinkUsed == true ->
                    getString(R.string.transaction_z_link_channel)
                courseBenefit.isInvoicePayment ->
                    getString(R.string.transaction_invoice_channel)
                else ->
                    getString(R.string.transaction_stepik_channel)
            }

        transactionPercentageValue.text = getString(R.string.transaction_share_value, courseBeneficiary.percent.removeSuffix(PERCENTAGE_SUFFIX))
        transactionIncomeValue.text = displayPriceMapper.mapToDisplayPrice(courseBenefit.currencyCode, courseBenefit.amount)
    }
}