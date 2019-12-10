package org.stepik.android.view.profile.ui.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.activities.contracts.CloseButtonInToolbar
import org.stepic.droid.util.AppConstants
import org.stepik.android.view.profile.ui.fragment.ProfileFragment

class ProfileActivity : SingleFragmentActivity(), CloseButtonInToolbar {
    companion object {
        private const val EXTRA_USER_ID = "user_id"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        fun createIntent(context: Context, userId: Long): Intent =
            Intent(context, ProfileActivity::class.java)
                .putExtra(EXTRA_USER_ID, userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && intent.action == AppConstants.OPEN_SHORTCUT_PROFILE) {
            analytic.reportEvent(Analytic.Shortcut.OPEN_PROFILE)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                getSystemService(ShortcutManager::class.java)?.reportShortcutUsed(AppConstants.PROFILE_SHORTCUT_ID)
            }
        }
    }

    override fun createFragment(): Fragment {
        val userId = intent
            .getLongExtra(EXTRA_USER_ID, 0)
            .takeIf { it > 0 }
            ?: getUserId(intent.data)

        return ProfileFragment.newInstance(userId)
    }

    private fun getUserId(dataUri: Uri?): Long {
        if (dataUri == null) return 0
        analytic.reportEvent(Analytic.Profile.OPEN_BY_LINK)
        return try {
            dataUri.pathSegments[1].toLong()
        } catch (exception: NumberFormatException) {
            -1
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.push_down)
    }
}
