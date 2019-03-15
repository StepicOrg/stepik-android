package org.stepik.android.view.profile_edit.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.profile_edit.ProfileEditPasswordPresenter
import org.stepik.android.presentation.profile_edit.ProfileEditPasswordView
import javax.inject.Inject

class ProfileEditPasswordActivity : AppCompatActivity(), ProfileEditPasswordView {
    private lateinit var profileEditPasswordPresenter: ProfileEditPasswordPresenter

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit_password)

        injectComponent()
        profileEditPasswordPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileEditPasswordPresenter::class.java)

        initCenteredToolbar(R.string.profile_edit_password_title, showHomeButton = true)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            false
        }
}