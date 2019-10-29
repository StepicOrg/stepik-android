package org.stepik.android.view.personal_deadlines.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import org.stepik.android.view.personal_deadlines.ui.adapters.LearningRateAdapter
import javax.inject.Inject

class LearningRateDialog : DialogFragment() {
    companion object {
        const val KEY_LEARNING_RATE = "hours_per_week"
        const val LEARNING_RATE_REQUEST_CODE = 3994

        const val TAG = "learning_rate_dialog"

        fun newInstance(): DialogFragment =
            LearningRateDialog()
    }

    @Inject
    lateinit var analytic: Analytic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val adapter = LearningRateAdapter(LearningRate.values(), this::selectLearningRate)

        val dialog = MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .title(R.string.deadlines_create_title)
                .adapter(adapter, LinearLayoutManager(context))
                .build()

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider_h)!!)
        dialog.recyclerView.addItemDecoration(divider)
        return dialog
    }

    private fun selectLearningRate(learningRate: LearningRate) {
        targetFragment?.onActivityResult(
                LEARNING_RATE_REQUEST_CODE,
                Activity.RESULT_OK,
                Intent().putExtra(KEY_LEARNING_RATE, learningRate as Parcelable)
        )
        val hoursValue = learningRate.millisPerWeek / AppConstants.MILLIS_IN_1HOUR
        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINE_MODE_CHOSEN, Bundle().apply {
            putLong(Analytic.Deadlines.Params.HOURS, hoursValue)
        })
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Deadlines.PERSONAL_DEADLINE_CREATED,
            mapOf(AmplitudeAnalytic.Deadlines.Params.HOURS to hoursValue))
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINE_MODE_CLOSED)
    }
}