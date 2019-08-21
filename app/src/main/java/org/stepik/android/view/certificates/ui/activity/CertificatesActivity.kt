package org.stepik.android.view.certificates.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_certificates.*
import kotlinx.android.synthetic.main.empty_certificates.*
import kotlinx.android.synthetic.main.error_no_connection.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.ui.dialogs.CertificateShareDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.certificates.CertificatesPresenter
import org.stepik.android.presentation.certificates.CertificatesView
import org.stepik.android.view.certificates.ui.adapter.CertificatesAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter
import javax.inject.Inject

class CertificatesActivity : FragmentActivityBase(), CertificatesView {
    companion object {
        private const val EXTRA_USER_ID = "user_id"

        fun createIntent(context: Context, userId: Long): Intent =
            Intent(context, CertificatesActivity::class.java)
                .putExtra(EXTRA_USER_ID, userId)
    }

    private var userId: Long = -1

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var certificatesPresenter: CertificatesPresenter

    private var certificatesAdapter: DefaultDelegateAdapter<CertificateViewItem> = DefaultDelegateAdapter()

    private val viewStateDelegate =
        ViewStateDelegate<CertificatesView.State>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificates)

        injectComponent()
        certificatesPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CertificatesPresenter::class.java)

        initCenteredToolbar(R.string.certificates_title, showHomeButton = true)
        userId = intent.getLongExtra(EXTRA_USER_ID, -1)

        initViewStateDelegate()

        certificateRecyclerView.apply {
            adapter = certificatesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        certificatesAdapter += CertificatesAdapterDelegate(
            onItemClick = { screenManager.showPdfInBrowserByGoogleDocs(this, it) },
            onShareButtonClick = { onNeedShowShareDialog(it) }
        )

        certificateSwipeRefresh.setOnRefreshListener { certificatesPresenter.onLoadCertificates(userId) }
        goToCatalog.setOnClickListener { screenManager.showCatalog(this) }

        certificatesPresenter.onLoadCertificates(userId)
    }

    private fun injectComponent() {
        App.component()
            .certificatesComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        certificatesPresenter.attachView(this)
    }

    override fun onStop() {
        certificatesPresenter.detachView(this)
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<CertificatesView.State.EmptyCertificates>(reportEmptyCertificates)
        viewStateDelegate.addState<CertificatesView.State.Loading>(loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<CertificatesView.State.NetworkError>(reportProblem)
        viewStateDelegate.addState<CertificatesView.State.CertificatesLoaded>(certificateSwipeRefresh, certificateRecyclerView)
    }

    override fun setState(state: CertificatesView.State) {
        certificateSwipeRefresh.isRefreshing = false
        certificateSwipeRefresh.isEnabled = (state is CertificatesView.State.CertificatesLoaded || state is CertificatesView.State.NetworkError)
        viewStateDelegate.switchState(state)
        when (state) {
            is CertificatesView.State.CertificatesLoaded -> {
                certificatesAdapter.items = state.certificates
            }
        }
    }

    private fun onNeedShowShareDialog(certificateViewItem: CertificateViewItem?) {
        if (certificateViewItem == null) {
            return
        }
        val supportFragmentManager = supportFragmentManager
            ?.takeIf { (it.findFragmentByTag(CertificateShareDialogFragment.TAG) == null) }
            ?: return
        val bottomSheetDialogFragment = CertificateShareDialogFragment.newInstance(certificateViewItem)
        bottomSheetDialogFragment.show(supportFragmentManager, CertificateShareDialogFragment.TAG)
    }
}