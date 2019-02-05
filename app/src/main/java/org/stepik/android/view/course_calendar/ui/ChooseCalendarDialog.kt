package org.stepik.android.view.course_calendar.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.model.CalendarItem
import java.util.*

class ChooseCalendarDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendarItems = arguments?.getParcelableArrayList<CalendarItem>(parcelableArrayListKey) ?: arrayListOf()
        val ownerTitles: Array<CharSequence> = calendarItems.map { it.owner }.toTypedArray()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.choose_calendar_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok) { _, _ ->
                    dialog.dismiss()
                    val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                    val chosenCalendarItem = calendarItems[selectedPosition]
                    (targetFragment as CallbackContract).onCalendarChosen(chosenCalendarItem)
                }
                .setSingleChoiceItems(ownerTitles, 0, null)
        return builder.create()
    }

    companion object {
        private const val parcelableArrayListKey = "parcelable_list_key"

        fun newInstance(primariesCalendars: ArrayList<CalendarItem>): ChooseCalendarDialog {
            val args = Bundle()
            args.putParcelableArrayList(parcelableArrayListKey, primariesCalendars)
            val fragment = ChooseCalendarDialog()
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * The callback should be implemented by caller fragment
     */
    interface CallbackContract {
        fun onCalendarChosen(calendarItem: CalendarItem)
    }
}