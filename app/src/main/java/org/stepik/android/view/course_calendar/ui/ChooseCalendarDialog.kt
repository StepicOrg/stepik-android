package org.stepik.android.view.course_calendar.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.stepic.droid.R
import org.stepik.android.domain.calendar.model.CalendarItem

class ChooseCalendarDialog : DialogFragment() {

    companion object {
        private const val parcelableArrayListKey = "parcelable_list_key"
        const val KEY_CALENDAR_ITEM = "calendar_item"
        const val CHOOSE_CALENDAR_REQUEST_CODE = 3203

        const val TAG = "choose_calendar_dialog"

        fun newInstance(primariesCalendars: List<CalendarItem>): ChooseCalendarDialog {
            val args = Bundle()
            args.putParcelableArrayList(parcelableArrayListKey, ArrayList(primariesCalendars))
            val fragment = ChooseCalendarDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendarItems = arguments?.getParcelableArrayList<CalendarItem>(parcelableArrayListKey) ?: arrayListOf()
        val ownerTitles: Array<String> = calendarItems.map { it.owner }.toTypedArray()
        val adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_single_choice, ownerTitles)

        val builder = AlertDialog.Builder(requireContext())
        builder
            .setTitle(R.string.choose_calendar_title)
            .setSingleChoiceItems(adapter, 0, null)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                val chosenCalendarItem = calendarItems[selectedPosition]
                targetFragment?.onActivityResult(
                        CHOOSE_CALENDAR_REQUEST_CODE,
                        Activity.RESULT_OK,
                        Intent().putExtra(KEY_CALENDAR_ITEM, chosenCalendarItem as Parcelable)
                )
            }
        return builder.create()
    }
}