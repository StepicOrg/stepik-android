package org.stepik.android.view.profile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.profile.ProfilePresenter
import org.stepik.android.presentation.profile.ProfileView
import javax.inject.Inject

class ProfileFragment : Fragment(), ProfileView {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var profilePresenter: ProfilePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        profilePresenter = ViewModelProviders.of(this, viewModelFactory).get(ProfilePresenter::class.java)
    }

    private fun injectComponent() {
        App.component()
            .profileComponentBuilderNew()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onStart() {
        super.onStart()
        profilePresenter.attachView(this)
    }

    override fun onStop() {
        profilePresenter.detachView(this)
        super.onStop()
    }
}