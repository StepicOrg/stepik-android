package org.stepik.android.view.course_calendar.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R

class ExplainCalendarPermissionDialog : DialogFragment() {

    companion object {
        const val TAG = "explain_permission_calendar_dialog"
        const val REQUEST_CALENDAR_PERMISSION = 1122

        fun newInstance(): DialogFragment =
            ExplainCalendarPermissionDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.allow_question)
            .setMessage(R.string.explain_calendar_permission)
            .setPositiveButton(R.string.allow) { _, _ ->
                (targetFragment as Callback)
                    .onCalendarPermissionChosen(true)
            }
            .setNegativeButton(R.string.deny) { _, _ ->
                (targetFragment as Callback)
                    .onCalendarPermissionChosen(false)
            }
            .create()

    interface Callback {
        fun onCalendarPermissionChosen(isAgreed: Boolean)
    }
}