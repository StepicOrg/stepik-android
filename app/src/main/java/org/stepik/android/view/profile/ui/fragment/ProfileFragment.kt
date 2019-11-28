package org.stepik.android.view.profile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.header_profile.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.profile.ProfilePresenter
import org.stepik.android.presentation.profile.ProfileView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject
import kotlin.math.min

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
        setHasOptionsMenu(true)
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

        (activity as? AppCompatActivity)
            ?.apply { setSupportActionBar(toolbar) }
            ?.supportActionBar
            ?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowTitleEnabled(false)
            }

        profileName.text = "Konstantin Konstantin"
        profileBio.text = "Saint Petersburg State University, Bioinformatics Institute, VK, Saint Petersburg State University, Bioinformatics Institute, VK, Saint Petersburg State University, Bioinformatics Institute, VK"

        toolbarTitle.text = profileName.text
        toolbarTitle.translationY = 1000f

        Glide
            .with(this)
            .load("https://i.pinimg.com/originals/a2/de/39/a2de3954697c636276192afea0a6f661.jpg")
            .into(profileImage)

        ViewCompat.setElevation(header, resources.getDimension(R.dimen.profile_header_elevation))

        scrollContainer.setOnScrollChangeListener { _: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
            ViewCompat.setElevation(appbar, if (scrollY > header.height) ViewCompat.getElevation(header) else 0f)

            val scroll = min(toolbar.height, scrollY)
            toolbarTitle.translationY = toolbar.height.toFloat() - scroll
        }
    }

    private fun injectComponent() {
        App.component()
            .profileComponentBuilderNew()
            .build()
            .inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
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