package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import biz.kasual.materialnumberpicker.MaterialNumberPicker
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
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

        return MaterialDialog.Builder(requireContext())
            .theme(Theme.LIGHT)
            .title(R.string.choose_language)
            .customView(picker, false)
            .positiveText(R.string.choose_action)
            .negativeText(R.string.cancel)
            .onPositive { _, _ ->
                val callback = if (parentFragment != null) {
                    parentFragment as Callback
                } else {
                    activity as Callback
                }
                callback.onLanguageChosen(picker.displayedValues[picker.value])
            }
            .build()
    }
}
