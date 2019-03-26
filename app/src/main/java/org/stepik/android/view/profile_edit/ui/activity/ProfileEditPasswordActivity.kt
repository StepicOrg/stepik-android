package org.stepik.android.view.profile_edit.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_profile_edit_password.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.profile_edit.ProfileEditPasswordPresenter
import org.stepik.android.presentation.profile_edit.ProfileEditPasswordView
import javax.inject.Inject

class ProfileEditPasswordActivity : AppCompatActivity(), ProfileEditPasswordView {
    companion object {
        private const val EXTRA_PROFILE_ID = "profile_id"

        fun createIntent(context: Context, profileId: Long): Intent =
            Intent(context, ProfileEditPasswordActivity::class.java)
                .putExtra(EXTRA_PROFILE_ID, profileId)
    }

    private lateinit var profileEditPasswordPresenter: ProfileEditPasswordPresenter

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val profileId by lazy { intent.getLongExtra(EXTRA_PROFILE_ID, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit_password)

        injectComponent()
        profileEditPasswordPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileEditPasswordPresenter::class.java)

        initCenteredToolbar(R.string.profile_edit_password_title, showHomeButton = true, homeIndicator = R.drawable.ic_close_dark)
    }

    private fun injectComponent() {
        App.component()
            .profileEditComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        profileEditPasswordPresenter.attachView(this)
    }

    override fun onStop() {
        profileEditPasswordPresenter.detachView(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.profile_edit_save -> {
                submit()
                true
            }
            else -> false
        }

    private fun submit() {
        val currentPassword = currentPasswordEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()
        val newPasswordAgain = newPasswordAgainEditText.text.toString()

        profileEditPasswordPresenter
            .updateProfilePassword(profileId, currentPassword, newPassword)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}