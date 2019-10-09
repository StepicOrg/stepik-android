package org.stepik.android.view.profile_edit.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_profile_edit_password.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.presentation.profile_edit.ProfileEditPasswordPresenter
import org.stepik.android.presentation.profile_edit.ProfileEditPasswordView
import org.stepik.android.view.profile_edit.ui.util.ValidateUtil
import javax.inject.Inject

class ProfileEditPasswordActivity : AppCompatActivity(), ProfileEditPasswordView {
    companion object {
        const val REQUEST_CODE = 12992

        private const val EXTRA_PROFILE_ID = "profile_id"

        fun createIntent(context: Context, profileId: Long): Intent =
            Intent(context, ProfileEditPasswordActivity::class.java)
                .putExtra(EXTRA_PROFILE_ID, profileId)
    }

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

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

        currentPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentPasswordInputLayout.isErrorEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        newPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                newPasswordInputLayout.isErrorEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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
        val isCurrentPasswordFilled = ValidateUtil.validateRequiredField(currentPasswordInputLayout, currentPasswordEditText)
        val isNewPasswordFilled = ValidateUtil.validateRequiredField(newPasswordInputLayout, newPasswordEditText)

        if (!isCurrentPasswordFilled ||
            !isNewPasswordFilled) {
            return
        }

        val currentPassword = currentPasswordEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()

        profileEditPasswordPresenter
            .updateProfilePassword(profileId, currentPassword, newPassword)
    }

    override fun setState(state: ProfileEditPasswordView.State) {
        when (state) {
            ProfileEditPasswordView.State.IDLE ->
                ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)

            ProfileEditPasswordView.State.LOADING ->
                ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)

            ProfileEditPasswordView.State.COMPLETE -> {
                ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun showNetworkError() {
        root.snackbar(messageRes = R.string.no_connection)
    }

    override fun showPasswordError() {
        root.snackbar(messageRes = R.string.profile_edit_error_password)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.push_down)
    }
}