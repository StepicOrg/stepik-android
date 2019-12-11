package org.stepik.android.view.profile_certificates.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import kotlinx.android.synthetic.main.fragment_profile_certificates.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.model.CertificateViewItem
import org.stepik.android.model.Certificate
import org.stepik.android.presentation.profile_certificates.ProfileCertificatesPresenter
import org.stepik.android.presentation.profile_certificates.ProfileCertificatesView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class ProfileCertificatesFragment : Fragment(), ProfileCertificatesView {
    companion object {
        private const val CERTIFICATES_TO_DISPLAY = 4

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

    private lateinit var certificatesPresenter: ProfileCertificatesPresenter
    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileCertificatesView.State>
    private lateinit var certificatesAdapter: DefaultDelegateAdapter<CertificateViewItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        injectComponent()

        certificatesPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileCertificatesPresenter::class.java)

        certificatesAdapter = DefaultDelegateAdapter()
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_profile_certificates, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ProfileCertificatesView.State.Idle>()
        viewStateDelegate.addState<ProfileCertificatesView.State.SilentLoading>()
        viewStateDelegate.addState<ProfileCertificatesView.State.Loading>(view)
        viewStateDelegate.addState<ProfileCertificatesView.State.Error>(view, certificatesLoadingError)
        viewStateDelegate.addState<ProfileCertificatesView.State.CertificatesLoaded>(view, certificatesRecycler)
        viewStateDelegate.addState<ProfileCertificatesView.State.NoCertificates>()

        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }
        certificatesTitle.setOnClickListener { screenManager.showCertificates(requireContext(), userId) }

        certificatesRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        certificatesRecycler.isNestedScrollingEnabled = false
        certificatesRecycler.adapter = certificatesAdapter

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

    private fun onCertificateClicked(item: Certificate) {

    }

    override fun setState(state: ProfileCertificatesView.State) {
        viewStateDelegate.switchState(state)

        when (state) {
            is ProfileCertificatesView.State.Loading -> {
                userId = state.userId
            }
            is ProfileCertificatesView.State.CertificatesLoaded -> {
                certificatesAdapter.items = state.certificates
                userId = state.userId
            }
        }
    }
}