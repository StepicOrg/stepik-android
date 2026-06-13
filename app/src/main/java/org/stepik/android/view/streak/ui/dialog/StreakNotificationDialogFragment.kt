package org.stepik.android.view.streak.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.databinding.HeaderStreakSuggestionDialogBinding
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class StreakNotificationDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "StreakSuggestionDialogFragment"

        fun newInstance(title: String, message: String, positiveEvent: String): DialogFragment =
            StreakNotificationDialogFragment()
                .apply {
                    this.title = title
                    this.message = message
                    this.positiveEvent = positiveEvent
                }
    }

    @Inject
    lateinit var analytic: Analytic

    private var title: String by argument()
    private var message: String by argument()
    private var positiveEvent: String by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        App.component().inject(this)

        val titleBinding = HeaderStreakSuggestionDialogBinding
            .inflate(LayoutInflater.from(context))
            .apply {
                headerTitle.text = title
            }

        return MaterialAlertDialogBuilder(requireContext())
            .setCustomTitle(titleBinding.root)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ ->
                analytic.reportEvent(positiveEvent)
                (activity as? Callback)
                    ?.onStreakNotificationDialogAccepted()
            }
            .setNegativeButton(R.string.later_tatle) { _, _ ->
                (activity as? Callback)
                    ?.onStreakNotificationDialogCancelled()
            }
            .create()
    }

    interface Callback {
        fun onStreakNotificationDialogAccepted()
        fun onStreakNotificationDialogCancelled()
    }
}
