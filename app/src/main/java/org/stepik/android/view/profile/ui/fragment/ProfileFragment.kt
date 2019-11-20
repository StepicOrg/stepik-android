package org.stepik.android.view.profile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_profile.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog
import org.stepic.droid.util.ProfileSettingsHelper
import org.stepic.droid.viewmodel.ProfileSettingsViewModel
import org.stepik.android.presentation.profile.ProfilePresenter
import org.stepik.android.presentation.profile.ProfileView
import org.stepik.android.view.profile.ui.adapter.ProfileSettingsAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class ProfileFragment : Fragment(), ProfileView {

    companion object {
        fun newInstance(): Fragment = ProfileFragment()
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var profilePresenter: ProfilePresenter

    private var profileSettingsAdapter: DefaultDelegateAdapter<ProfileSettingsViewModel> = DefaultDelegateAdapter()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupProfileSettingsAdapter()
        with(profileSettingsRecyclerView) {
            adapter = profileSettingsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        }
    }

    override fun onStart() {
        super.onStart()
        profilePresenter.attachView(this)
    }

    override fun onStop() {
        profilePresenter.detachView(this)
        super.onStop()
    }

    private fun setupProfileSettingsAdapter() {
        profileSettingsAdapter.items = ProfileSettingsHelper.getProfileSettings()
        profileSettingsAdapter += ProfileSettingsAdapterDelegate(
            onItemClick = {
                when (it) {
                    R.string.settings_title -> {
                        analytic.reportEvent(Analytic.Screens.USER_OPEN_SETTINGS)
                        screenManager.showSettings(requireActivity())
                    }

                    R.string.downloads -> {
                        analytic.reportEvent(Analytic.Screens.USER_OPEN_DOWNLOADS)
                        screenManager.showDownloads(requireContext())
                    }

                    R.string.feedback_title -> {
                        analytic.reportEvent(Analytic.Screens.USER_OPEN_FEEDBACK)
                        screenManager.openFeedbackActivity(requireActivity())
                    }

                    R.string.about_app_title -> {
                        analytic.reportEvent(Analytic.Screens.USER_OPEN_ABOUT_APP)
                        screenManager.openAboutActivity(requireActivity())
                    }

                    R.string.logout_title -> {
                        val supportFragmentManager = activity
                            ?.supportFragmentManager
                            ?: return@ProfileSettingsAdapterDelegate

                        val dialog = LogoutAreYouSureDialog.newInstance()
                        dialog.showIfNotExists(supportFragmentManager, LogoutAreYouSureDialog.TAG)
                        analytic.reportEvent(Analytic.Screens.USER_LOGOUT)
                    }
                }
            }
        )
    }
}