package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import kotlinx.android.synthetic.main.view_code_editor.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.model.code.CodeOptions
import org.stepic.droid.ui.activities.CodePlaygroundActivity
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.ColorUtil

class CodePlaygroundFragment : FragmentBase(),
        OnBackClickListener,
        ProgrammingLanguageChooserDialogFragment.Callback,
        ResetCodeDialogFragment.Callback {

    companion object {
        private const val CODE_KEY = "code_key"
        private const val LANG_KEY = "lang_key"
        private const val CODE_OPTIONS_KEY = "code_options_key"
        fun newInstance(code: String, lang: String, codeOptions: CodeOptions): CodePlaygroundFragment {
            val args = Bundle()
            args.putString(CODE_KEY, code)
            args.putString(LANG_KEY, lang)
            args.putParcelable(CODE_OPTIONS_KEY, codeOptions)
            val fragment = CodePlaygroundFragment()
            fragment.arguments = args
            return fragment
        }

    }

    private var currentLanguage: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (activity as? BackButtonHandler)?.setBackClickListener(this)
    }

    override fun onDetach() {
        (activity as? BackButtonHandler)?.removeBackClickListener(this)
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = arguments.getString(LANG_KEY)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_code_playground, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.code_playground_title, true)
        if (savedInstanceState == null) {
            codeEditor.setText(arguments.getString(CODE_KEY))
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.code_playground_menu, menu)

        val menuItem = menu?.findItem(R.id.action_reset_code)
        val resetString = SpannableString(getString(R.string.code_quiz_reset))
        resetString.setSpan(ForegroundColorSpan(ColorUtil.getColorArgb(R.color.new_red_color)), 0, resetString.length, 0)
        menuItem?.title = resetString
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_reset_code -> {
            val dialog = ResetCodeDialogFragment.newInstance()
            if (!dialog.isAdded) {
                dialog.show(childFragmentManager, null)
            }
            true
        }
        R.id.action_language_code -> {
            arguments.getParcelable<CodeOptions>(CODE_OPTIONS_KEY)
                    ?.limits
                    ?.keys
                    ?.sorted()
                    ?.toTypedArray()
                    ?.let {
                        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(it)
                        if (!dialog.isAdded) {
                            dialog.show(childFragmentManager, null)
                        }
                    }
            true
        }
        else -> false
    }


    override fun onReset() {
        currentLanguage?.let { lang ->
            val template = arguments.getParcelable<CodeOptions>(CODE_OPTIONS_KEY)?.codeTemplates?.get(lang)
            codeEditor.setText(template)
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        currentLanguage = programmingLanguage
        codeEditor.setText(arguments.getParcelable<CodeOptions>(CODE_OPTIONS_KEY).codeTemplates[programmingLanguage])
    }


    override fun onBackClick(): Boolean {
        val resultIntent = Intent()
        resultIntent.putExtra(CodePlaygroundActivity.LANG_KEY, currentLanguage)
        resultIntent.putExtra(CodePlaygroundActivity.CODE_KEY, codeEditor.text.toString())
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        return false
    }

}
