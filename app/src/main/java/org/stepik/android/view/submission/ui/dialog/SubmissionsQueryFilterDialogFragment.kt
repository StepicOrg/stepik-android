package org.stepik.android.view.submission.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.BottomSheetDialogSubmissionsFilterBinding
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import org.stepik.android.model.Submission
import ru.nobird.android.view.base.ui.extension.argument

class SubmissionsQueryFilterDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "SubmissionsQueryFilterDialogFragment"

        fun newInstance(submissionsFilterQuery: SubmissionsFilterQuery, isPeerReview: Boolean): DialogFragment =
            SubmissionsQueryFilterDialogFragment().apply {
                this.submissionsFilterQuery = submissionsFilterQuery
                this.isPeerReview = isPeerReview
            }
    }

    private var submissionsFilterQuery: SubmissionsFilterQuery by argument()
    private var isPeerReview: Boolean by argument()

    private lateinit var defaultStatusButton: AppCompatRadioButton
    private lateinit var defaultDateSortButton: AppCompatRadioButton
    private lateinit var defaultReviewStatusButton: AppCompatRadioButton

    private lateinit var submissionStatusRadioButtons: List<AppCompatRadioButton>
    private lateinit var dateSortRadioButtons: List<AppCompatRadioButton>
    private lateinit var reviewStatusRadioButtons: List<AppCompatRadioButton>
    private lateinit var allRadioButtons: List<AppCompatRadioButton>

    private val binding: BottomSheetDialogSubmissionsFilterBinding by viewBinding(BottomSheetDialogSubmissionsFilterBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_BottomSheetDialog)
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_submissions_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultStatusButton = binding.anyStatusButton
        defaultDateSortButton = binding.descendingDateSortButton
        defaultReviewStatusButton = binding.anyReviewStatusButton

        submissionStatusRadioButtons = listOf<AppCompatRadioButton>(binding.anyStatusButton, binding.correctStatusButton, binding.incorrectStatusButton)
        dateSortRadioButtons = listOf<AppCompatRadioButton>(binding.descendingDateSortButton, binding.ascendingDateSortButton)
        reviewStatusRadioButtons = listOf<AppCompatRadioButton>(binding.anyReviewStatusButton, binding.finishedReviewStatusButton, binding.awaitingReviewStatusButton)
        allRadioButtons = submissionStatusRadioButtons + dateSortRadioButtons + reviewStatusRadioButtons

        binding.reviewStatusTitle.isVisible = isPeerReview
        binding.anyReviewStatusButtonDivider.root.isVisible = isPeerReview
        binding.reviewStatusClosingDivider.root.isVisible = isPeerReview
        reviewStatusRadioButtons.forEach { it.isVisible = isPeerReview }

        setupFilters(submissionsFilterQuery)

        binding.dismissSubmissionsFilter.isVisible = isMustShowDismiss()

        setupListeners(submissionStatusRadioButtons)
        setupListeners(dateSortRadioButtons)
        setupListeners(reviewStatusRadioButtons)

        binding.dismissSubmissionsFilter.setOnClickListener {
            allRadioButtons.forEach { radioButton -> radioButton.isChecked = false }
            defaultStatusButton.isChecked = true
            defaultDateSortButton.isChecked = true
            defaultReviewStatusButton.isChecked = true
            it.isVisible = false
        }

        binding.applyFilterAction.setOnClickListener {
            val newFilter = mapFiltersToQuery()
            if (newFilter != submissionsFilterQuery) {
                (parentFragment as? Callback)
                    ?.onSyncFilterQueryWithParent(newFilter)
            }
            dismiss()
        }
    }

    private fun setupListeners(radioButtons: List<AppCompatRadioButton>) {
        radioButtons.forEach {
            it.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    onRadioButtonClicked(buttonView, radioButtons)
                    binding.dismissSubmissionsFilter.isVisible = isMustShowDismiss()
                }
            }
        }
    }

    private fun setupFilters(submissionsFilterQuery: SubmissionsFilterQuery) {
        val submissionStatusRadioButton = when (submissionsFilterQuery.status) {
            Submission.Status.CORRECT.scope ->
                binding.correctStatusButton

            Submission.Status.WRONG.scope ->
                binding.incorrectStatusButton

            else ->
                binding.anyStatusButton
        }

        submissionStatusRadioButton.isChecked = true

        val dateSortRadioButton = if (submissionsFilterQuery.order == SubmissionsFilterQuery.Order.ASC) {
            binding.ascendingDateSortButton
        } else {
            binding.descendingDateSortButton
        }

        dateSortRadioButton.isChecked = true

        val reviewStatusRadioButton = when (submissionsFilterQuery.reviewStatus) {
            SubmissionsFilterQuery.ReviewStatus.AWAITING ->
                binding.awaitingReviewStatusButton

            SubmissionsFilterQuery.ReviewStatus.DONE ->
                binding.finishedReviewStatusButton

            else ->
                binding.anyReviewStatusButton
        }

        reviewStatusRadioButton.isChecked = true
    }

    private fun mapFiltersToQuery(): SubmissionsFilterQuery {
        val status = when {
            binding.correctStatusButton.isChecked ->
                Submission.Status.CORRECT?.scope

            binding.incorrectStatusButton.isChecked ->
                Submission.Status.WRONG?.scope

            else ->
                null
        }

        val dateOrder = if (binding.ascendingDateSortButton.isChecked) {
            SubmissionsFilterQuery.Order.ASC
        } else {
            SubmissionsFilterQuery.Order.DESC
        }

        val reviewStatus = when {
            binding.awaitingReviewStatusButton.isChecked ->
                SubmissionsFilterQuery.ReviewStatus.AWAITING

            binding.finishedReviewStatusButton.isChecked ->
                SubmissionsFilterQuery.ReviewStatus.DONE

            else ->
                null
        }

        return submissionsFilterQuery.copy(
            order = dateOrder,
            status = status,
            reviewStatus = reviewStatus
        )
    }

    private fun onRadioButtonClicked(buttonView: CompoundButton, radioButtons: List<AppCompatRadioButton>) {
        radioButtons.forEach {
            if (it.id != buttonView.id) {
                it.isChecked = false
            }
        }
    }

    private fun isMustShowDismiss(): Boolean =
        allRadioButtons.any {
            if (it.id != defaultStatusButton.id &&
                it.id != defaultDateSortButton.id &&
                it.id != defaultReviewStatusButton.id
            ) {
                it.isChecked
            } else {
                false
            }
        }

    interface Callback {
        fun onSyncFilterQueryWithParent(submissionsFilterQuery: SubmissionsFilterQuery)
    }
}