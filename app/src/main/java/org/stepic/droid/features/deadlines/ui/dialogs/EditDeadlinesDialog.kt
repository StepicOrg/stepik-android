package org.stepic.droid.features.deadlines.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import org.stepic.droid.R
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.model.Section
import org.stepic.droid.web.storage.model.StorageRecord

class EditDeadlinesDialog: DialogFragment() {
    companion object {
        const val TAG = "edit_deadlines_dialog"

        const val KEY_SECTIONS = "sections"
        const val KEY_DEADLINES = "deadlines"

        const val EDIT_DEADLINES_REQUEST_CODE = 3993

        fun newInstance(sections: List<Section>, deadlinesRecord: StorageRecord<DeadlinesWrapper>): EditDeadlinesDialog {
            val fragment = EditDeadlinesDialog()
            fragment.arguments = Bundle().apply {
                putParcelableArrayList(KEY_SECTIONS, ArrayList(sections))
                putParcelable(KEY_DEADLINES, deadlinesRecord.data)
            }
            return fragment
        }
    }

    private lateinit var sections: ArrayList<Section>
    private lateinit var deadlinesWrapper: DeadlinesWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sections = savedInstanceState?.getParcelableArrayList(KEY_SECTIONS) ?: arguments.getParcelableArrayList(KEY_SECTIONS)
        deadlinesWrapper = savedInstanceState?.getParcelable(KEY_DEADLINES) ?: arguments.getParcelable(KEY_DEADLINES)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        return MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .title(R.string.deadlines_edit_title)
                .customView(recyclerView, false)
                .positiveText(R.string.save)
                .negativeText(R.string.cancel)
                .onPositive { _, _ ->
                    saveResults()
                }
                .build()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putParcelableArrayList(KEY_SECTIONS, sections)
            putParcelable(KEY_DEADLINES, deadlinesWrapper)
        }
    }

    private fun saveResults() {
        targetFragment?.onActivityResult(
                EDIT_DEADLINES_REQUEST_CODE,
                Activity.RESULT_OK,
                Intent().putExtra(KEY_DEADLINES, deadlinesWrapper)
        )
    }
}