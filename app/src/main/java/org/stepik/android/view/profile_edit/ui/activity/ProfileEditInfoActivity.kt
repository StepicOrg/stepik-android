package org.stepik.android.view.profile_edit.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.util.initCenteredToolbar
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

    private lateinit var profileEditInfoPresenter: ProfileEditInfoPresenter

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val profile by lazy { intent.getParcelableExtra<Profile>(EXTRA_PROFILE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit_password)

        injectComponent()
        profileEditInfoPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileEditInfoPresenter::class.java)

        initCenteredToolbar(R.string.profile_edit_info_title, showHomeButton = true, homeIndicator = R.drawable.ic_close_dark)
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
            else -> false
        }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}