package org.stepik.android.view.profile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.header_profile.*
import org.stepik.android.presentation.profile.ProfilePresenter
import org.stepik.android.presentation.profile.ProfileView
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class ProfileFragment : Fragment() {
    companion object {
        fun newInstance(): Fragment =
            ProfileFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var profilePresenter: ProfilePresenter


    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profilePresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfilePresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()

        profileName.text = "Konstantin Konstantin"
        profileBio.text = "Saint Petersburg State University, Bioinformatics Institute, VK"

        Glide
            .with(this)
            .load("https://i.pinimg.com/originals/a2/de/39/a2de3954697c636276192afea0a6f661.jpg")
            .into(profileImage)
    }

    private fun injectComponent() {
        App.component()
            .profileComponentBuilderNew()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()

//        profilePresenter.attachView(this)
    }

    override fun onStop() {
//        profilePresenter.detachView(this)
        super.onStop()
    }

//    override fun setState(state: ProfileView.State) {
//        when (state) {
//
//        }
//    }
}