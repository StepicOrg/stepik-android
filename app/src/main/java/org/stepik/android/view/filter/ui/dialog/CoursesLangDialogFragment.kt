package org.stepik.android.view.filter.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.model.StepikFilter
import org.stepik.android.presentation.filter.FiltersPresenter
import org.stepik.android.presentation.filter.FiltersView
import java.util.EnumSet
import javax.inject.Inject

class CoursesLangDialogFragment : DialogFragment(), FiltersView {
    companion object {
        const val TAG = "CoursesLangDialogFragment"

        fun newInstance(): DialogFragment =
            CoursesLangDialogFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val presenter: FiltersPresenter by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    private fun injectComponent() {
        App.component()
            .filterComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.language_of_courses)
            .setSingleChoiceItems(resources.getStringArray(R.array.course_list_languages), -1) { _, which ->
                val filters = EnumSet.noneOf(StepikFilter::class.java)
                when (which) {
                    0 -> filters.add(StepikFilter.RUSSIAN)
                    1 -> filters.add(StepikFilter.ENGLISH)
                }
                presenter.onFilterChanged(filters)
                dismiss()
            }
            .create()

    override fun setState(state: FiltersView.State) {
        if (state is FiltersView.State.FiltersLoaded) {
            val selection = when {
                StepikFilter.RUSSIAN in state.filters -> 0
                StepikFilter.ENGLISH in state.filters -> 1
                else -> -1
            }
            (dialog as? AlertDialog)
                ?.listView
                ?.setItemChecked(selection, true)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }
}