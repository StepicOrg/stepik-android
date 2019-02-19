package org.stepik.android.view.course_calendar.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepik.android.domain.calendar.model.CalendarItem
import java.util.*

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
        val ownerTitles: Array<CharSequence> = calendarItems.map { it.owner }.toTypedArray()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.choose_calendar_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok) { _, _ ->
                    dialog.dismiss()
                    val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                    val chosenCalendarItem = calendarItems[selectedPosition]
                    targetFragment?.onActivityResult(
                            CHOOSE_CALENDAR_REQUEST_CODE,
                            Activity.RESULT_OK,
                            Intent().putExtra(KEY_CALENDAR_ITEM, chosenCalendarItem as Parcelable)
                    )
                }
                .setSingleChoiceItems(ownerTitles, 0, null)
        return builder.create()
    }
}