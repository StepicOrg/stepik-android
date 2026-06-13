package org.stepik.android.view.course_revenue.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.databinding.BottomSheetDialogTransactionBinding
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.model.user.User
import org.stepik.android.view.course_revenue.mapper.RevenuePriceMapper
import ru.nobird.android.view.base.ui.extension.argument
import java.util.TimeZone
import java.util.Currency
import java.text.DecimalFormat
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

    private val binding: BottomSheetDialogTransactionBinding by viewBinding(BottomSheetDialogTransactionBinding::bind)

    private var courseBenefit: CourseBenefit by argument()
    private var courseBeneficiary: CourseBeneficiary by argument()
    private var user: User? = null
    private var courseTitle: String? = null

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var revenuePriceMapper: RevenuePriceMapper

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

        val currency = Currency.getInstance(courseBenefit.currencyCode)
        val decimalFormat = DecimalFormat().apply { setCurrency(currency) }
        decimalFormat.minimumFractionDigits = 2

        binding.transactionTitle.text =
            if (courseBenefit.status == CourseBenefit.Status.DEBITED) {
                getString(R.string.transaction_title_purchase)
            } else {
                getString(R.string.transaction_title_refund)
            }
        binding.transactionDateValue.text = DateTimeHelper.getPrintableDate(courseBenefit.time, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())

        binding.transactionCourseValue.text = courseTitle.orEmpty()
        binding.transactionCourseTitle.isVisible = courseTitle != null
        binding.transactionCourseValue.isVisible = courseTitle != null

        binding.transactionBuyerValue.text = user?.fullName.orEmpty()
        binding.transactionBuyerTitle.isVisible = user != null
        binding.transactionBuyerValue.isVisible = user != null
        binding.buyerOverlayView.setOnClickListener { user?.let { screenManager.openProfile(requireContext(), it.id) } }

        binding.transactionPaymentValue.text =
            revenuePriceMapper.mapToDisplayPrice(courseBenefit.currencyCode, decimalFormat.format(courseBenefit.paymentAmount?.toDoubleOrNull() ?: 0.0))

        binding.transactionPromoCodeValue.text = courseBenefit.promoCode.orEmpty()
        binding.transactionPromoCodeTitle.isVisible = courseBenefit.promoCode != null
        binding.transactionPromoCodeValue.isVisible = courseBenefit.promoCode != null

        val isChannelInfoVisible = courseBenefit.status == CourseBenefit.Status.DEBITED ||
            (courseBenefit.buyer == null && !courseBenefit.isInvoicePayment)

        binding.transactionChannelTitle.isVisible = isChannelInfoVisible
        binding.transactionChannelValue.isVisible = isChannelInfoVisible
        binding.transactionChannelValue.text =
            when {
                courseBenefit.isZLinkUsed == true ->
                    getString(R.string.transaction_a_link_channel)
                courseBenefit.isInvoicePayment ->
                    getString(R.string.transaction_invoice_channel)
                courseBenefit.buyer == null && !courseBenefit.isInvoicePayment -> {
                    buildString {
                        append(getString(R.string.transaction_manual_channel))
                        if (courseBenefit.description != null) {
                            append(": ${courseBenefit.description}")
                        }
                    }
                }
                else ->
                    getString(R.string.transaction_stepik_channel)
            }

        binding.transactionPercentageValue.text = getString(R.string.transaction_share_value, courseBeneficiary.percent.removeSuffix(PERCENTAGE_SUFFIX))
        binding.transactionIncomeValue.text = revenuePriceMapper.mapToDisplayPrice(
            courseBenefit.currencyCode,
            decimalFormat.format(courseBenefit.amount.toDoubleOrNull() ?: 0.0),
            debitPrefixRequired = courseBenefit.status == CourseBenefit.Status.DEBITED
        )
    }
}
