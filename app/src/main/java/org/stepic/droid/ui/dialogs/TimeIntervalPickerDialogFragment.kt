package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.NumberPicker
import biz.kasual.materialnumberpicker.MaterialNumberPicker
import com.afollestad.materialdialogs.MaterialDialog
import org.stepic.droid.R
import org.stepic.droid.ui.util.TimeIntervalUtil
import timber.log.Timber


class TimeIntervalPickerDialogFragment : DialogFragment() {
    companion object {
        private val chosenPositionKey = "chosenPositionKey"
        fun newInstance(): android.support.v4.app.DialogFragment {
            val fragment = TimeIntervalPickerDialogFragment()
            return fragment
        }
    }

    var picker: MaterialNumberPicker? = null

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(chosenPositionKey, picker?.value ?: TimeIntervalUtil.middle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        picker = MaterialNumberPicker(context)
        picker?.minValue = 0
        picker?.maxValue = TimeIntervalUtil.values.size - 1
        picker?.displayedValues = TimeIntervalUtil.values
        picker?.value = savedInstanceState?.getInt(chosenPositionKey) ?: TimeIntervalUtil.middle
        picker?.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        picker?.wrapSelectorWheel = false
        try {
            picker?.setTextSize(50f) //TODO: Warning: reflection!
        } catch (exception: Exception) {
            Timber.e("reflection failed -> ignore")
        }

        return MaterialDialog.Builder(activity)
                .title(R.string.notification_time)
                .customView(picker!!, false)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive { dialog, which ->
                    //todo set result to Ok with position
                }
                .onNegative { materialDialog, dialogAction ->
                    //todo: set negative result cancel (just like cancel)
                }
                .cancelListener {
                    //todo this analytic include onNegative
                }
                .build()
    }
}
