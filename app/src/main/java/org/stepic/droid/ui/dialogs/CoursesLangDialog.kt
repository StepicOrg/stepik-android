package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.FiltersPresenter
import org.stepic.droid.core.presenters.contracts.FiltersView
import org.stepic.droid.model.StepikFilter
import java.util.EnumSet
import javax.inject.Inject

class CoursesLangDialog : DialogFragment(), FiltersView {
    companion object {
        fun newInstance(): DialogFragment =
            CoursesLangDialog()
    }

    @Inject
    lateinit var filtersPresenter: FiltersPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val lang = arrayOf(
            getString(R.string.language_ru_filter),
            getString(R.string.language_en_filter)
        )

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.language_of_courses)
            .setSingleChoiceItems(lang, -1) { _, which ->
                val filters = EnumSet.noneOf(StepikFilter::class.java)
                when(which) {
                    0 -> filters.add(StepikFilter.RUSSIAN)
                    1 -> filters.add(StepikFilter.ENGLISH)
                }
                filtersPresenter.onFilterChanged(filters)
                dismiss()
            }
            .create()
    }

    override fun onStart() {
        super.onStart()
        filtersPresenter.attachView(this)
        filtersPresenter.onNeedFilters()
    }

    override fun onStop() {
        filtersPresenter.detachView(this)
        super.onStop()
    }

    override fun onFiltersPrepared(filters: EnumSet<StepikFilter>) {
        val position = when {
            StepikFilter.RUSSIAN in filters -> 0
            StepikFilter.ENGLISH in filters -> 1
            else -> -1
        }

        (dialog as? AlertDialog)
            ?.listView
            ?.setItemChecked(position, true)
    }
}