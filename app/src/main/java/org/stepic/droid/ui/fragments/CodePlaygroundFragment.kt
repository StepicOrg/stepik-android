package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_code_playground.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.ui.activities.CodePlaygroundActivity
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.ColorUtil

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
        setHasOptionsMenu(true)
    }

    override fun onBackClick(): Boolean {
        val resultIntent = Intent()
        resultIntent.putExtra(CodePlaygroundActivity.LANG_KEY, currentLanguage)
        resultIntent.putExtra(CodePlaygroundActivity.CODE_KEY, codePlaygroundEditText.text.toString())
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        return false
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
            Toast.makeText(context, "reset", Toast.LENGTH_SHORT).show()
            true
        }
        R.id.action_language_code -> {
            Toast.makeText(context, "lang", Toast.LENGTH_SHORT).show()
            true
        }
        else -> false
    }

}
