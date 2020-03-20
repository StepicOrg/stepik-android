package org.stepik.android.view.personal_deadlines.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_deadlines.model.Deadline
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.model.Section
import org.stepik.android.view.personal_deadlines.ui.adapters.EditDeadlinesAdapter
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class EditDeadlinesDialog : DialogFragment() {
    companion object {
        const val TAG = "edit_deadlines_dialog"

        const val KEY_SECTIONS = "sections"
        const val KEY_DEADLINES = "deadlines"

        const val EDIT_DEADLINES_REQUEST_CODE = 3993

        private const val DATE_PICKER_TAG = "date_picker"
        private const val KEY_EDITED_SECTION_ID = "edited_section_id"

        fun newInstance(sections: List<Section>, deadlinesRecord: StorageRecord<DeadlinesWrapper>): EditDeadlinesDialog {
            val fragment = EditDeadlinesDialog()
            fragment.arguments = Bundle(2).apply {
                putParcelableArrayList(KEY_SECTIONS, ArrayList(sections))
                putParcelableArrayList(KEY_DEADLINES, ArrayList(deadlinesRecord.data.deadlines))
            }
            return fragment
        }
    }

    @Inject
    lateinit var analytic: Analytic

    private lateinit var sections: ArrayList<Section>
    private lateinit var deadlines: ArrayList<Deadline>

    private lateinit var adapter: EditDeadlinesAdapter

    private var editedSectionId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        App.component().inject(this)

        super.onCreate(savedInstanceState)

        sections = savedInstanceState?.getParcelableArrayList(KEY_SECTIONS) ?: arguments?.getParcelableArrayList(KEY_SECTIONS)!!
        deadlines = savedInstanceState?.getParcelableArrayList(KEY_DEADLINES) ?: arguments?.getParcelableArrayList(KEY_DEADLINES)!!

        editedSectionId = savedInstanceState?.getLong(KEY_EDITED_SECTION_ID, -1) ?: -1
        if (editedSectionId != -1L) {
            restorePickerListener()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val recyclerView = RecyclerView(context)
        recyclerView.isVerticalFadingEdgeEnabled = true
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = EditDeadlinesAdapter(sections, deadlines) {
            showDatePickerForDeadline(it)
        }
        recyclerView.adapter = adapter

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.bg_divider_vertical)!!)
        recyclerView.addItemDecoration(divider)

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.deadlines_edit_title)
            .setView(recyclerView)
            .setPositiveButton(R.string.save) { _, _ ->
                saveResults()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
            .apply {
                isCancelable = false
                setCanceledOnTouchOutside(false)
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putParcelableArrayList(KEY_SECTIONS, sections)
            putParcelableArrayList(KEY_DEADLINES, deadlines)

            putLong(KEY_EDITED_SECTION_ID, editedSectionId)
        }
    }

    private fun showDatePickerForDeadline(deadline: Deadline) {
        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINE_TIME_OPENED)

        val datePicker = MaterialDatePicker
            .Builder
            .datePicker()
            .setTitleText(R.string.deadlines_edit_date_picker_title)
            .setSelection(deadline.deadline.time)
            .build()

        editedSectionId = deadline.section
        setDatePickerListener(datePicker, deadline.section)
        datePicker.showIfNotExists(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun restorePickerListener() {
        val pickerDialogFragment = childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<Long>
        if (pickerDialogFragment != null) {
            setDatePickerListener(pickerDialogFragment, editedSectionId)
        } else {
            editedSectionId = -1
        }
    }

    private fun setDatePickerListener(datePicker: MaterialDatePicker<Long>, sectionId: Long) {
        datePicker.addOnPositiveButtonClickListener { time ->
            val calendar = Calendar.getInstance()
            calendar.time = Date(time)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            adapter.updateDeadline(Deadline(sectionId, calendar.time))
            editedSectionId = -1
        }

        datePicker.addOnDismissListener {
            editedSectionId = -1
            analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINE_TIME_CLOSED)
        }
    }

    private fun saveResults() {
        targetFragment?.onActivityResult(
                EDIT_DEADLINES_REQUEST_CODE,
                Activity.RESULT_OK,
                Intent().putParcelableArrayListExtra(KEY_DEADLINES, deadlines)
        )
    }
}