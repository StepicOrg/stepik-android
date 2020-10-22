package org.stepik.android.view.profile_edit.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_profile_edit_info.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.model.user.Profile
import org.stepik.android.presentation.profile_edit.ProfileEditInfoPresenter
import org.stepik.android.presentation.profile_edit.ProfileEditInfoView
import javax.inject.Inject

class ProfileEditInfoActivity : AppCompatActivity(), ProfileEditInfoView {
    companion object {
        const val REQUEST_CODE = 12090

        private const val EXTRA_PROFILE = "profile"

        fun createIntent(context: Context, profile: Profile): Intent =
            Intent(context, ProfileEditInfoActivity::class.java)
                .putExtra(EXTRA_PROFILE, profile)
    }

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private val profileEditInfoPresenter: ProfileEditInfoPresenter by viewModels { viewModelFactory }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val profile by lazy { intent.getParcelableExtra<Profile>(EXTRA_PROFILE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit_info)

        injectComponent()

        initCenteredToolbar(R.string.profile_edit_info_title, showHomeButton = true, homeIndicator = R.drawable.ic_close_dark)

        if (savedInstanceState == null) {
            firstNameEditText.setText(profile.firstName ?: "")
            lastNameEditText.setText(profile.lastName ?: "")

            shortBioEditText.setText(profile.shortBio ?: "")
            detailsEditText.setText(profile.details ?: "")
        }
    }

    private fun injectComponent() {
        App.component()
            .profileEditComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        profileEditInfoPresenter.attachView(this)
    }

    override fun onStop() {
        profileEditInfoPresenter.detachView(this)
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
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val shortBio = shortBioEditText.text.toString()
        val details = detailsEditText.text.toString()

        profileEditInfoPresenter.updateProfileInfo(profile, firstName, lastName, shortBio, details)
    }

    override fun setState(state: ProfileEditInfoView.State) {
        when (state) {
            ProfileEditInfoView.State.IDLE ->
                ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)

            ProfileEditInfoView.State.LOADING ->
                ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)

            ProfileEditInfoView.State.COMPLETE -> {
                ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun showNetworkError() {
        root.snackbar(messageRes = R.string.no_connection)
    }

    override fun showInfoError() {
        root.snackbar(messageRes = R.string.profile_edit_error_info)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.push_down)
    }
}