package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import biz.kasual.materialnumberpicker.MaterialNumberPicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.ui.util.initForCodeLanguages

class ProgrammingLanguageChooserDialogFragment : DialogFragment() {
    companion object {
        private const val LANGUAGES_KEY = "languages_key"
        fun newInstance(languages: Array<String>): ProgrammingLanguageChooserDialogFragment {
            val args = Bundle()
            args.putStringArray(LANGUAGES_KEY, languages)
            val fragment = ProgrammingLanguageChooserDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    interface Callback {
        fun onLanguageChosen(programmingLanguage: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val picker = MaterialNumberPicker(context)
        picker.initForCodeLanguages(arguments?.getStringArray(LANGUAGES_KEY)!!)

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.choose_language)
            .setView(picker)
            .setPositiveButton(R.string.choose_action) { _, _ ->
                (parentFragment as Callback).onLanguageChosen(picker.displayedValues[picker.value])
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
}
