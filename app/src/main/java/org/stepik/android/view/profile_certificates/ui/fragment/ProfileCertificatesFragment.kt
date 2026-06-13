package org.stepik.android.view.profile_certificates.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.model.CertificateListItem
import org.stepic.droid.databinding.FragmentProfileCertificatesBinding
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
    private val binding: FragmentProfileCertificatesBinding by viewBinding(FragmentProfileCertificatesBinding::bind)
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
        viewStateDelegate.addState<ProfileCertificatesView.State.Loading>(view, binding.profileCertificatesRecycler)
        viewStateDelegate.addState<ProfileCertificatesView.State.Error>(view, binding.certificatesLoadingError.root)
        viewStateDelegate.addState<ProfileCertificatesView.State.CertificatesCache>(view, binding.profileCertificatesRecycler)
        viewStateDelegate.addState<ProfileCertificatesView.State.CertificatesRemote>(view, binding.profileCertificatesRecycler)
        viewStateDelegate.addState<ProfileCertificatesView.State.NoCertificates>()

        binding.certificatesLoadingError.tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }
        binding.profileCertificatesTitle.setOnClickListener { screenManager.showCertificates(requireContext(), profileId, isCurrentUser) }

        binding.profileCertificatesRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.profileCertificatesRecycler.isNestedScrollingEnabled = false
        binding.profileCertificatesRecycler.adapter = certificatesAdapter

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
            else -> Unit
        }
    }
}