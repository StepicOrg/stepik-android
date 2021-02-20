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
import kotlinx.android.synthetic.main.bottom_sheet_dialog_submissions_filter.*
import org.stepic.droid.R
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import org.stepik.android.model.Submission
import ru.nobird.android.view.base.ui.extension.argument

class SubmissionsQueryFilterDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "SubmissionsQueryFilterDialogFragment"

        fun newInstance(submissionsFilterQuery: SubmissionsFilterQuery): DialogFragment =
            SubmissionsQueryFilterDialogFragment().apply {
                this.submissionsFilterQuery = submissionsFilterQuery
            }
    }

    private var submissionsFilterQuery: SubmissionsFilterQuery by argument()

    private lateinit var defaultStatusButton: AppCompatRadioButton
    private lateinit var defaultDateSortButton: AppCompatRadioButton
    private lateinit var defaultReviewStatusButton: AppCompatRadioButton

    private lateinit var submissionStatusRadioButtons: List<AppCompatRadioButton>
    private lateinit var dateSortRadioButtons: List<AppCompatRadioButton>
    private lateinit var reviewStatusRadioButtons: List<AppCompatRadioButton>
    private lateinit var allRadioButtons: List<AppCompatRadioButton>

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

        defaultStatusButton = anyStatusButton
        defaultDateSortButton = descendingDateSortButton
        defaultReviewStatusButton = anyReviewStatusButton

        submissionStatusRadioButtons = listOf<AppCompatRadioButton>(anyStatusButton, correctStatusButton, partiallyCorrectButton, incorrectStatusButton)
        dateSortRadioButtons = listOf<AppCompatRadioButton>(descendingDateSortButton, ascendingDateSortButton)
        reviewStatusRadioButtons = listOf<AppCompatRadioButton>(anyReviewStatusButton, finishedReviewStatusButton, awaitingReviewStatusButton)
        allRadioButtons = submissionStatusRadioButtons + dateSortRadioButtons + reviewStatusRadioButtons

        setupFilters(submissionsFilterQuery)

        dismissSubmissionsFilter.isVisible = isMustShowDismiss()

        setupListeners(submissionStatusRadioButtons)
        setupListeners(dateSortRadioButtons)
        setupListeners(reviewStatusRadioButtons)

        dismissSubmissionsFilter.setOnClickListener {
            allRadioButtons.forEach { radioButton -> radioButton.isChecked = false }
            defaultStatusButton.isChecked = true
            defaultDateSortButton.isChecked = true
            defaultReviewStatusButton.isChecked = true
            it.isVisible = false
        }

        applyFilterAction.setOnClickListener {
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
                    dismissSubmissionsFilter.isVisible = isMustShowDismiss()
                }
            }
        }
    }

    private fun setupFilters(submissionsFilterQuery: SubmissionsFilterQuery) {
        val submissionStatusRadioButton = when (submissionsFilterQuery.status) {
            Submission.Status.CORRECT.scope ->
                correctStatusButton

            Submission.Status.PARTIALLY_CORRECT.scope ->
                partiallyCorrectButton

            Submission.Status.WRONG.scope ->
                incorrectStatusButton

            else ->
                anyStatusButton
        }

        submissionStatusRadioButton.isChecked = true

        val dateSortRadioButton = if (submissionsFilterQuery.order == SubmissionsFilterQuery.Order.ASC) {
            ascendingDateSortButton
        } else {
            descendingDateSortButton
        }

        dateSortRadioButton.isChecked = true

        val reviewStatusRadioButton = when (submissionsFilterQuery.reviewStatus) {
            SubmissionsFilterQuery.ReviewStatus.AWAITING ->
                awaitingReviewStatusButton

            SubmissionsFilterQuery.ReviewStatus.DONE ->
                finishedReviewStatusButton

            else ->
                anyReviewStatusButton
        }

        reviewStatusRadioButton.isChecked = true
    }

    private fun mapFiltersToQuery(): SubmissionsFilterQuery {
        val status = when {
            correctStatusButton.isChecked ->
                Submission.Status.CORRECT?.scope

            partiallyCorrectButton.isChecked ->
                Submission.Status.PARTIALLY_CORRECT?.scope

            incorrectStatusButton.isChecked ->
                Submission.Status.WRONG?.scope

            else ->
                null
        }

        val dateOrder = if (ascendingDateSortButton.isChecked) {
            SubmissionsFilterQuery.Order.ASC
        } else {
            SubmissionsFilterQuery.Order.DESC
        }

        val reviewStatus = when {
            awaitingReviewStatusButton.isChecked ->
                SubmissionsFilterQuery.ReviewStatus.AWAITING

            finishedReviewStatusButton.isChecked ->
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