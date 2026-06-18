package org.stepik.android.view.filter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import dev.androidbroadcast.vbpd.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.stepic.droid.R
import org.stepic.droid.databinding.BottomSheetDialogFilterBinding
import org.stepic.droid.base.App
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.view.catalog.ui.fragment.CatalogFragment
import ru.nobird.app.core.model.safeCast
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class FilterBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private val binding: BottomSheetDialogFilterBinding by viewBinding(BottomSheetDialogFilterBinding::bind)

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
        radioButtons = listOf<AppCompatRadioButton>(binding.anyRadioButton, binding.rusRadioButton, binding.engRadioButton)
        compoundButtons = radioButtons + listOf(binding.certificatesSwitch, binding.freeSwitch)

        setupFilters(filterQuery)

        binding.dismissFilter.isVisible = isMustShowDismiss()

        radioButtons.forEach {
            it.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    onRadioButtonClicked(buttonView)
                    binding.dismissFilter.isVisible = isMustShowDismiss()
                }
            }
        }

        binding.certificatesSwitch.setOnCheckedChangeListener { _, _ ->
            binding.dismissFilter.isVisible = isMustShowDismiss()
        }

        binding.freeSwitch.setOnCheckedChangeListener { _, _ ->
            binding.dismissFilter.isVisible = isMustShowDismiss()
        }

        binding.dismissFilter.setOnClickListener {
            compoundButtons.forEach { compoundButton ->  compoundButton.isChecked = false }
            defaultLanguageRadioButton.isChecked = true
            it.isVisible = false
        }

        binding.applyFilterAction.setOnClickListener {
            val newFilterQuery = mapFiltersToQuery()
            if (newFilterQuery != filterQuery || parentFragment is CatalogFragment) {
                (activity.safeCast<Callback>() ?: parentFragment.safeCast<Callback>())
                    ?.onSyncFilterQueryWithParent(newFilterQuery)
            }
            dismiss()
        }
    }

    private fun setupFilters(filterQuery: CourseListFilterQuery) {
        /**
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
         **/
    }

    private fun mapFiltersToQuery(): CourseListFilterQuery =
        CourseListFilterQuery()

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
                binding.rusRadioButton

            StepikFilter.ENGLISH.language ->
                binding.engRadioButton

            else ->
                throw IllegalStateException()
        }

    interface Callback {
        fun onSyncFilterQueryWithParent(filterQuery: CourseListFilterQuery)
    }
}