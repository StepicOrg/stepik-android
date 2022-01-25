package org.stepik.android.view.profile_certificates.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import kotlinx.android.synthetic.main.fragment_profile_certificates.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.model.CertificateListItem
import org.stepik.android.presentation.profile_certificates.ProfileCertificatesPresenter
import org.stepik.android.presentation.profile_certificates.ProfileCertificatesView
import org.stepik.android.view.certificate.ui.adapter.delegate.CertificateProfileAdapterDelegate
import org.stepik.android.view.certificate.ui.adapter.delegate.CertificatesProfilePlaceholderAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class ProfileCertificatesFragment : Fragment(R.layout.fragment_profile_certificates), ProfileCertificatesView {
    companion object {
        fun newInstance(userId: Long): Fragment =
            ProfileCertificatesFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    private var userId: Long by argument()
    private var isCurrentUser: Boolean = false

    private val certificatesPresenter: ProfileCertificatesPresenter by viewModels { viewModelFactory }
    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileCertificatesView.State>
    private lateinit var certificatesAdapter: DefaultDelegateAdapter<CertificateListItem>

    private var profileId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        certificatesAdapter = DefaultDelegateAdapter()
        certificatesAdapter += CertificatesProfilePlaceholderAdapterDelegate()
        certificatesAdapter += CertificateProfileAdapterDelegate(::onCertificateClicked)
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ProfileCertificatesView.State.Idle>()
        viewStateDelegate.addState<ProfileCertificatesView.State.SilentLoading>()
        viewStateDelegate.addState<ProfileCertificatesView.State.Loading>(view, profileCertificatesRecycler)
        viewStateDelegate.addState<ProfileCertificatesView.State.Error>(view, certificatesLoadingError)
        viewStateDelegate.addState<ProfileCertificatesView.State.CertificatesCache>(view, profileCertificatesRecycler)
        viewStateDelegate.addState<ProfileCertificatesView.State.CertificatesRemote>(view, profileCertificatesRecycler)
        viewStateDelegate.addState<ProfileCertificatesView.State.NoCertificates>()

        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }
        profileCertificatesTitle.setOnClickListener { screenManager.showCertificates(requireContext(), profileId, isCurrentUser) }

        profileCertificatesRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        profileCertificatesRecycler.isNestedScrollingEnabled = false
        profileCertificatesRecycler.adapter = certificatesAdapter

        setDataToPresenter()
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        certificatesPresenter.showCertificatesForUser(forceUpdate)
    }

    override fun onStart() {
        super.onStart()
        certificatesPresenter.attachView(this)
    }

    override fun onStop() {
        certificatesPresenter.detachView(this)
        super.onStop()
    }

    private fun onCertificateClicked(path: String) {
        screenManager.showPdfInBrowserByGoogleDocs(requireActivity(), path)
    }

    override fun setState(state: ProfileCertificatesView.State) {
        viewStateDelegate.switchState(state)

        when (state) {
            is ProfileCertificatesView.State.Loading -> {
                certificatesAdapter.items = listOf(
                    CertificateListItem.Placeholder,
                    CertificateListItem.Placeholder,
                    CertificateListItem.Placeholder
                )
                profileId = state.profileData.user.id
                isCurrentUser = state.profileData.isCurrentUser
            }
            is ProfileCertificatesView.State.CertificatesCache -> {
                certificatesAdapter.items = state.certificates
                profileId = state.profileData.user.id
                isCurrentUser = state.profileData.isCurrentUser
            }
            is ProfileCertificatesView.State.CertificatesRemote -> {
                certificatesAdapter.items = state.certificates
                profileId = state.profileData.user.id
                isCurrentUser = state.profileData.isCurrentUser
            }
        }
    }
}