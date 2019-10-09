package org.stepik.android.view.course_calendar.ui

import android.app.Dialog
import android.os.Bundle
import androidx.core.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R

class ExplainCalendarPermissionDialog : DialogFragment() {

    companion object {
        const val TAG = "explain_permission_calendar_dialog"
        const val REQUEST_CALENDAR_PERMISSION = 1122

        fun newInstance(): DialogFragment =
            ExplainCalendarPermissionDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(R.string.allow_question)
                .setMessage(R.string.explain_calendar_permission)
                .setPositiveButton(R.string.allow) { _, _ ->
                    (targetFragment as Callback).onCalendarPermissionChosen(true)
                }
                .setNegativeButton(R.string.deny) { _, _ ->
                    (targetFragment as Callback).onCalendarPermissionChosen(false)
                }
        return builder.create()
    }

    interface Callback {
        fun onCalendarPermissionChosen(isAgreed: Boolean)
    }
}