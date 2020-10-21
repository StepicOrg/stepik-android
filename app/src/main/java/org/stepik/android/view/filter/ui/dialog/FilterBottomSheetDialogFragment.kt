package org.stepik.android.view.filter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_filter.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class FilterBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "FilterBottomSheetDialogFragment"

        fun newInstance(filterQuery: CourseListFilterQuery): DialogFragment =
            FilterBottomSheetDialogFragment().apply {
                this.filterQuery = filterQuery
            }
    }

    private var filterQuery: CourseListFilterQuery by argument()
    private lateinit var defaultLanguageRadioButton: AppCompatRadioButton
    private lateinit var radioButtons: List<AppCompatRadioButton>
    private lateinit var compoundButtons: List<CompoundButton>

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private fun injectComponent() {
        App.component().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_BottomSheetDialog)
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultLanguageRadioButton = obtainDefaultLanguageRadioButton()
        radioButtons = listOf<AppCompatRadioButton>(anyRadioButton, rusRadioButton, engRadioButton)
        compoundButtons = radioButtons + listOf(certificatesSwitch, freeSwitch)

        setupFilters(filterQuery)

        dismissFilter.isVisible = isMustShowDismiss()

        radioButtons.forEach {
            it.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    onRadioButtonClicked(buttonView)
                    dismissFilter.isVisible = isMustShowDismiss()
                }
            }
        }

        certificatesSwitch.setOnCheckedChangeListener { _, _ ->
            dismissFilter.isVisible = isMustShowDismiss()
        }

        freeSwitch.setOnCheckedChangeListener { _, _ ->
            dismissFilter.isVisible = isMustShowDismiss()
        }

        dismissFilter.setOnClickListener {
            compoundButtons.forEach { compoundButton ->  compoundButton.isChecked = false }
            defaultLanguageRadioButton.isChecked = true
            it.isVisible = false
        }

        applyFilterAction.setOnClickListener {
            val newFilterQuery = mapFiltersToQuery()
            if (newFilterQuery != filterQuery) {
                (parentFragment as? Callback)
                    ?.onSyncFilterQueryWithParent(newFilterQuery)
            }
            dismiss()
        }
    }

    private fun setupFilters(filterQuery: CourseListFilterQuery) {
        val radioButton = when (filterQuery.language) {
            StepikFilter.RUSSIAN.language ->
                rusRadioButton

            StepikFilter.ENGLISH.language ->
                engRadioButton

            else ->
                anyRadioButton
        }

        radioButton.isChecked = true

        if (filterQuery.withCertificate != null) {
            certificatesSwitch.isChecked = true
        }

        if (filterQuery.isPaid == false) {
            freeSwitch.isChecked = true
        }
    }

    private fun mapFiltersToQuery(): CourseListFilterQuery {
        val language = when {
            rusRadioButton.isChecked ->
                StepikFilter.RUSSIAN.language

            engRadioButton.isChecked ->
                StepikFilter.ENGLISH.language

            anyRadioButton.isChecked ->
                null

            else ->
                null
        }

        val hasCertificate = if (certificatesSwitch.isChecked) {
            true
        } else {
            null
        }

        val isPaid = if (freeSwitch.isChecked) {
            false
        } else {
            null
        }

        return CourseListFilterQuery(language, isPaid, hasCertificate)
    }

    private fun onRadioButtonClicked(buttonView: CompoundButton) {
        radioButtons.forEach {
            if (it.id != buttonView.id) {
                it.isChecked = false
            }
        }
    }

    private fun isMustShowDismiss(): Boolean =
        compoundButtons.any {
            if (it.id != defaultLanguageRadioButton.id) {
                it.isChecked
            } else {
                false
            }
        }

    private fun obtainDefaultLanguageRadioButton(): AppCompatRadioButton =
        when (sharedPreferenceHelper.languageForFeatured) {
            StepikFilter.RUSSIAN.language ->
                rusRadioButton

            StepikFilter.ENGLISH.language ->
                engRadioButton

            else ->
                throw IllegalStateException()
        }

    interface Callback {
        fun onSyncFilterQueryWithParent(filterQuery: CourseListFilterQuery)
    }
}