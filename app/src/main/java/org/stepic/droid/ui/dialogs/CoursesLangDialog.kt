package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.ui.adapters.SingleChoiceAdapter
import org.stepik.android.presentation.catalog.FiltersPresenter
import org.stepik.android.presentation.catalog.FiltersView
import java.util.EnumSet
import javax.inject.Inject

class CoursesLangDialog: DialogFragment(), FiltersView {
    companion object {
        fun newInstance(): DialogFragment =
            CoursesLangDialog()
    }

    @Inject
    lateinit var filtersPresenter: FiltersPresenter

    private val adapter = SingleChoiceAdapter { pos ->
        val filters = EnumSet.noneOf(StepikFilter::class.java)
        when(pos) {
            0 -> filters.add(StepikFilter.RUSSIAN)
            1 -> filters.add(StepikFilter.ENGLISH)
        }
        filtersPresenter.onFilterChanged(filters)
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        adapter.data = listOf(
            getString(R.string.language_ru_filter),
            getString(R.string.language_en_filter)
        )

        return MaterialDialog.Builder(requireContext())
            .theme(Theme.LIGHT)
            .title(R.string.language_of_courses)
            .adapter(adapter, LinearLayoutManager(context))
            .build()
    }

    override fun setState(state: FiltersView.State) {
        TODO("Not yet implemented")
    }

    override fun onStart() {
        super.onStart()
        filtersPresenter.attachView(this)
//        filtersPresenter.onNeedFilters()
    }

    override fun onStop() {
        filtersPresenter.detachView(this)
        super.onStop()
    }

//    override fun onFiltersPrepared(filters: EnumSet<StepikFilter>) {
//        adapter.selection = when {
//            StepikFilter.RUSSIAN in filters -> 0
//            StepikFilter.ENGLISH in filters -> 1
//            else -> -1
//        }
//    }
}