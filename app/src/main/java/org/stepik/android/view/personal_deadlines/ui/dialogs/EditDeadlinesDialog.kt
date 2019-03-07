package org.stepik.android.view.personal_deadlines.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_deadlines.model.Deadline
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.model.Section
import org.stepik.android.view.personal_deadlines.ui.adapters.EditDeadlinesAdapter
import java.util.Calendar
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
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = EditDeadlinesAdapter(sections, deadlines) {
            showDatePickerForDeadline(it)
        }
        recyclerView.adapter = adapter

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider_h)!!)
        recyclerView.addItemDecoration(divider)

        return MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .title(R.string.deadlines_edit_title)
                .customView(recyclerView, false)
                .positiveText(R.string.save)
                .negativeText(R.string.cancel)
                .onPositive { _, _ ->
                    saveResults()
                }
                .build().apply {
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

        val calendar = DateTimeHelper.calendarFromLocalMillis(deadline.deadline.time)

        editedSectionId = deadline.section
        val pickerDialogFragment = CalendarDatePickerDialogFragment()
                .setThemeCustom(R.style.CalendarPickerDialogStyle)
                .setPreselectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        setDatePickerListener(pickerDialogFragment, deadline.section)
        pickerDialogFragment.show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun restorePickerListener() {
        val pickerDialogFragment = childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? CalendarDatePickerDialogFragment
        if (pickerDialogFragment != null) {
            setDatePickerListener(pickerDialogFragment, editedSectionId)
        } else {
            editedSectionId = -1
        }
    }

    private fun setDatePickerListener(pickerDialogFragment: CalendarDatePickerDialogFragment, sectionId: Long) {
        pickerDialogFragment.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            adapter.updateDeadline(Deadline(sectionId, calendar.time))
            editedSectionId = -1
        }.setOnDismissListener {
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