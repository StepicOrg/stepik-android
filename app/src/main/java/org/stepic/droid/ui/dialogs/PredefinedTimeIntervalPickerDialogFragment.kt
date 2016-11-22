package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.NumberPicker
import org.stepic.droid.R
import org.stepic.droid.ui.util.TimeIntervalUtil


class PredefinedTimeIntervalPickerDialogFragment : DialogFragment() {
    companion object {
        private val chosenPositionKey = "chosenPositionKey"
        fun newInstance(): android.support.v4.app.DialogFragment {
            val fragment = PredefinedTimeIntervalPickerDialogFragment()
            return fragment
        }
    }

    var picker: NumberPicker? = null

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(chosenPositionKey, picker?.value ?: TimeIntervalUtil.middle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        picker = NumberPicker(context)
        picker?.minValue = 0
        picker?.maxValue = TimeIntervalUtil.values.size - 1
        picker?.displayedValues = TimeIntervalUtil.values
        picker?.value = savedInstanceState?.getInt(chosenPositionKey) ?: TimeIntervalUtil.middle
        picker?.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        picker?.wrapSelectorWheel = false

        builder.setTitle(R.string.notification_time)
                .setView(picker)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    //todo set result to Ok with position
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                    //todo: set result cancel
                }

        return builder.create()
    }
}
