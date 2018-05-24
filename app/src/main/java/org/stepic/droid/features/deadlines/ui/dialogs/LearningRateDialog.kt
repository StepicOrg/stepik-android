package org.stepic.droid.features.deadlines.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import org.stepic.droid.R
import org.stepic.droid.features.deadlines.model.LearningRate
import org.stepic.droid.features.deadlines.ui.adapters.LearningRateAdapter

class LearningRateDialog: DialogFragment() {
    companion object {
        const val KEY_LEARNING_RATE = "hours_per_week"
        const val LEARNING_RATE_REQUEST_CODE = 3994

        const val TAG = "learning_rate_dialog"

        fun newInstance() = LearningRateDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val recyclerView = RecyclerView(context)
        recyclerView.adapter = LearningRateAdapter(LearningRate.values(), this::selectLearningRate)
        recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }

        return MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .title(R.string.deadlines_create_title)
                .customView(recyclerView, false)
                .negativeText(R.string.cancel)
                .build()
    }

    private fun selectLearningRate(learningRate: LearningRate) {
        targetFragment?.onActivityResult(
                LEARNING_RATE_REQUEST_CODE,
                Activity.RESULT_OK,
                Intent().putExtra(KEY_LEARNING_RATE, learningRate as Parcelable)
        )
    }
}