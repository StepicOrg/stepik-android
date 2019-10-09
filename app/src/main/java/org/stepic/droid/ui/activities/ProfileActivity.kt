package org.stepic.droid.ui.activities

import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Bundle
import androidx.core.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.activities.contracts.CloseButtonInToolbar
import org.stepic.droid.ui.fragments.ProfileFragment
import org.stepic.droid.util.AppConstants


class ProfileActivity : SingleFragmentActivity(),
        CloseButtonInToolbar {

    companion object {
        val optionalUserIdKey = "optionalUserIdKey"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.profile_title)
    }

    override fun createFragment(): Fragment {
        if (intent?.action?.equals(AppConstants.OPEN_SHORTCUT_PROFILE) ?: false) {
            analytic.reportEvent(Analytic.Shortcut.OPEN_PROFILE)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                val shortcutManager = getSystemService(ShortcutManager::class.java)
                shortcutManager.reportShortcutUsed(AppConstants.PROFILE_SHORTCUT_ID)
            }
        }

        val userIdInternal: Long? = intent?.extras?.getLong(optionalUserIdKey)
        if (userIdInternal != null && userIdInternal != 0L) {
            return ProfileFragment.newInstance(userIdInternal)
        } else {
            val dataUri = intent?.data
            val userId = getUserId(dataUri)
            return ProfileFragment.newInstance(userId)
        }
    }

    private fun getUserId(dataUri: Uri?): Long {
        if (dataUri == null) return 0;
        analytic.reportEvent(Analytic.Profile.OPEN_BY_LINK)
        val pathSegments = dataUri.pathSegments
        try {
            return pathSegments[1].toLong()
        } catch (exception: NumberFormatException) {
            return -1;
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish();
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
