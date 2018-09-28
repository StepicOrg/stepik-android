package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import kotlinx.android.synthetic.main.fragment_code_playground.*
import kotlinx.android.synthetic.main.view_code_editor_layout.*
import kotlinx.android.synthetic.main.view_code_toolbar.codeToolbarView
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.code.util.CodeToolbarUtil
import org.stepik.android.model.code.CodeOptions
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.ui.activities.CodePlaygroundActivity
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.ui.util.*
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.argument

class CodePlaygroundFragment : FragmentBase(),
        OnBackClickListener,
        ProgrammingLanguageChooserDialogFragment.Callback,
        ResetCodeDialogFragment.Callback,
        ChangeCodeLanguageDialog.Callback,
        CodeToolbarAdapter.OnSymbolClickListener {
    companion object {
        private const val ANALYTIC_SCREEN_TYPE: String = "fullscreen"

        fun newInstance(code: String, lang: String, codeOptions: CodeOptions) =
                CodePlaygroundFragment().also {
                    it.code = code
                    it.lang = lang
                    it.codeOptions = codeOptions
                }

    }

    private var currentLanguage: String? = null
    private var codeToolbarAdapter: CodeToolbarAdapter? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var wasReset: Boolean = false

    private var code: String by argument()
    private var lang: String by argument()
    private var codeOptions: CodeOptions by argument()

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
        codeToolbarAdapter = CodeToolbarAdapter(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_code_playground, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.code_playground_title, true)

        codeToolbarView.adapter = codeToolbarAdapter
        codeToolbarAdapter?.onSymbolClickListener = this
        codeToolbarView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        codeToolbarAdapter?.setLanguage(lang)
        codeEditor.codeToolbarAdapter = codeToolbarAdapter

        if (savedInstanceState == null) {
            codeEditor.setText(code)
        }
        currentLanguage?.let {
            codeEditor.lang = extensionForLanguage(it)
        }
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        onGlobalLayoutListener = listenKeyboardChanges(
                codePlaygroundRootView,
                onKeyboardShown = {
                    codeToolbarView?.visibility = View.VISIBLE
                },
                onKeyboardHidden = {
                    codeToolbarView?.visibility = View.GONE
                }
        )
    }

    override fun onStop() {
        super.onStop()
        stopListenKeyboardChanges(codePlaygroundRootView, onGlobalLayoutListener)
        onGlobalLayoutListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        codeToolbarAdapter?.onSymbolClickListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        codeToolbarAdapter = null
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
            analytic.reportEvent(Analytic.Code.CODE_RESET_PRESSED,
                    Bundle().apply { putString(AppConstants.ANALYTIC_CODE_SCREEN_KEY, ANALYTIC_SCREEN_TYPE) }
            )
            val dialog = ResetCodeDialogFragment.newInstance()
            if (!dialog.isAdded) {
                dialog.show(childFragmentManager, null)
            }
            true
        }
        R.id.action_language_code -> {
            val dialog = ChangeCodeLanguageDialog.newInstance()
            if (!dialog.isAdded) {
                dialog.show(childFragmentManager, null)
            }
            true
        }
        else -> false
    }

    override fun onReset() {
        currentLanguage?.let { lang ->
            wasReset = true
            codeEditor.setText(codeOptions.codeTemplates[lang])
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        wasReset = true
        currentLanguage = programmingLanguage
        codeToolbarAdapter?.setLanguage(programmingLanguage)
        codeEditor.setText(codeOptions.codeTemplates[programmingLanguage])
        codeEditor.lang = extensionForLanguage(programmingLanguage)
    }

    override fun onBackClick(): Boolean {
        val resultIntent = Intent()
        resultIntent.putExtra(CodePlaygroundActivity.WAS_RESET, wasReset)
        resultIntent.putExtra(CodePlaygroundActivity.LANG_KEY, currentLanguage)
        resultIntent.putExtra(CodePlaygroundActivity.CODE_KEY, codeEditor.text.toString())
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        return false
    }

    override fun onSymbolClick(symbol: String) {
        CodeToolbarUtil.reportSelectedSymbol(analytic, currentLanguage, symbol)
        codeEditor.insertText(CodeToolbarUtil.mapToolbarSymbolToPrintable(symbol, codeEditor.indentSize))
    }


    override fun onChangeLanguage() {
        showLanguageChoosingDialog()
    }

    private fun showLanguageChoosingDialog() {
        val languages = codeOptions.limits.keys.sorted().toTypedArray()

        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(languages)
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

}
