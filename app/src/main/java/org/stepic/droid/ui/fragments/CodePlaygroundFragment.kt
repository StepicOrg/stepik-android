package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_code_playground.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.ui.activities.CodePlaygroundActivity
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import org.stepic.droid.ui.util.initCenteredToolbar

class CodePlaygroundFragment : FragmentBase(), OnBackClickListener {
    companion object {
        private const val CODE_KEY = "code_key"
        private const val LANG_KEY = "lang_key"

        fun newInstance(code: String, lang: String): CodePlaygroundFragment {
            val args = Bundle()
            args.putString(CODE_KEY, code)
            args.putString(LANG_KEY, lang)
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
            codePlaygroundEditText.setText(arguments.getString(CODE_KEY))
        }
    }

    override fun onBackClick(): Boolean {
        val resultIntent = Intent()
        resultIntent.putExtra(CodePlaygroundActivity.LANG_KEY, currentLanguage)
        resultIntent.putExtra(CodePlaygroundActivity.CODE_KEY, codePlaygroundEditText.text.toString())
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        return false
    }

}
