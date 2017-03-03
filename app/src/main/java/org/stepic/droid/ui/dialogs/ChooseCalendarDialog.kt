package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.squareup.otto.Bus
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.events.CalendarChosenEvent
import org.stepic.droid.model.CalendarItem
import java.util.*
import javax.inject.Inject

class ChooseCalendarDialog : DialogFragment() {

    @Inject
    lateinit var bus: Bus

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        MainApplication.component().inject(this)
        val calendarItems = arguments.getParcelableArrayList<CalendarItem>(parcelableArrayListKey)
        val ownerTitles: Array<CharSequence> = calendarItems.map { it.owner }.toTypedArray()

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.choose_calendar_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, { _, _ ->
                    dialog.dismiss()
                    val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                    bus.post(CalendarChosenEvent(calendarItems[selectedPosition]))
                })
                .setSingleChoiceItems(ownerTitles, 0, null)
        return builder.create()
    }

    companion object {

        private val parcelableArrayListKey = "parcelable_list_key"

        fun newInstance(primariesCalendars: ArrayList<CalendarItem>): ChooseCalendarDialog {
            val args = Bundle()
            args.putParcelableArrayList(parcelableArrayListKey, primariesCalendars)
            val fragment = ChooseCalendarDialog()
            fragment.arguments = args
            return fragment
        }
    }
}