package org.stepic.droid.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.NumberPicker
import biz.kasual.materialnumberpicker.MaterialNumberPicker
import com.afollestad.materialdialogs.MaterialDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.TimeIntervalUtil
import timber.log.Timber
import javax.inject.Inject


class TimeIntervalPickerDialogFragment : DialogFragment() {
    companion object {
        val resultIntervalCodeKey = "resultIntervalCodeKey"
        private val chosenPositionKey = "chosenPositionKey"
        fun newInstance(): android.support.v4.app.DialogFragment =
                TimeIntervalPickerDialogFragment()
    }

    @Inject
    lateinit var sharedPreferences: SharedPreferenceHelper

    @Inject
    lateinit var analytic: Analytic

    lateinit var picker: MaterialNumberPicker

    init {
        App.component().inject(this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(chosenPositionKey, picker.value)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        picker = MaterialNumberPicker(context)
        picker.minValue = 0
        picker.maxValue = TimeIntervalUtil.values.size - 1
        picker.displayedValues = TimeIntervalUtil.values
        picker.value = savedInstanceState?.getInt(chosenPositionKey) ?: sharedPreferences.timeNotificationCode
        picker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        picker.wrapSelectorWheel = false
        try {
            picker.setTextSize(50f) //Warning: reflection!
        } catch (exception: Exception) {
            Timber.e("reflection failed -> ignore")
        }

        return MaterialDialog.Builder(activity)
                .title(R.string.choose_notification_time_interval)
                .customView(picker, false)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive { _, _ ->
                    //todo set result to Ok with position
                    val data = Intent()
                    data.putExtra(resultIntervalCodeKey, picker.value)
                    targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
                }
                .build()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null) // explicitly click Negative or cancel by back button || touch outside
    }

}
