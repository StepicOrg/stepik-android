package org.stepik.android.view.in_app_web_view.ui.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment

class InAppWebViewActivity : SingleFragmentActivity() {
    companion object {
        private val REVIEW_SESSIONS_PATTERN = "/review/sessions".toRegex()

        private const val EXTRA_TITLE = "title"
        private const val EXTRA_URL = "url"

        fun createIntent(context: Context, title: String, url: String): Intent =
            Intent(context, InAppWebViewActivity::class.java)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_URL, url)
    }

    override fun createFragment(): Fragment {
        val intentDataString = intent.dataString.orEmpty()

        val (title, url) = when {
            REVIEW_SESSIONS_PATTERN.containsMatchIn(intentDataString) ->
                getString(R.string.step_quiz_review_given_title) to intentDataString

            else ->
                intent.getStringExtra(EXTRA_TITLE).orEmpty() to intent.getStringExtra(EXTRA_URL).orEmpty()
        }

        return InAppWebViewDialogFragment.newInstance(
            title = title,
            url = url,
            isProvideAuth = true
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}