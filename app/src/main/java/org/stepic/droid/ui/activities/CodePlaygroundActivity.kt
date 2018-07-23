package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.model.code.CodeOptions
import org.stepic.droid.ui.fragments.CodePlaygroundFragment
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import java.lang.ref.WeakReference

class CodePlaygroundActivity : SingleFragmentActivity(), BackButtonHandler {

    companion object {
        const val CODE_KEY = "code_key"
        const val LANG_KEY = "lang_key"
        const val WAS_RESET = "WAS_RESET"
        private const val QUIZ_INFO_KEY = "quiz_info_key"

        fun intentForLaunch(callingActivity: Activity, code: String, lang: String, codeOptions: CodeOptions): Intent {
            val intent = Intent(callingActivity, CodePlaygroundActivity::class.java)
            intent.putExtra(CODE_KEY, code)
            intent.putExtra(LANG_KEY, lang)
            intent.putExtra(QUIZ_INFO_KEY, codeOptions)
            return intent
        }
    }

    private var onBackClickListener: WeakReference<OnBackClickListener>? = null

    override fun createFragment(): Fragment = CodePlaygroundFragment.newInstance(
            intent.getStringExtra(CODE_KEY),
            intent.getStringExtra(LANG_KEY),
            intent.getParcelableExtra<CodeOptions>(QUIZ_INFO_KEY))

    override fun applyTransitionPrev() {
        //no-op
    }


    override fun setBackClickListener(onBackClickListener: OnBackClickListener) {
        this.onBackClickListener = WeakReference(onBackClickListener)
    }

    override fun removeBackClickListener(onBackClickListener: OnBackClickListener) {
        this.onBackClickListener = null
    }

    override fun finish() {
        hideSoftKeypad()
        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (fragmentBackKeyIntercept()) {
                    return true
                } else {
                    finish()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        if (!fragmentBackKeyIntercept()) {
            super.onBackPressed()
        }
    }

    private fun fragmentBackKeyIntercept(): Boolean =
            onBackClickListener?.get()?.onBackClick() ?: false
}
