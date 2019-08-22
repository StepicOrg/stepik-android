package org.stepik.android.view.certificate.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_certificates.*
import kotlinx.android.synthetic.main.empty_certificates.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.ui.dialogs.CertificateShareDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.setTextColor
import org.stepik.android.presentation.certificate.CertificatesPresenter
import org.stepik.android.presentation.certificate.CertificatesView
import org.stepik.android.view.certificate.ui.adapter.CertificatesAdapterDelegate
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

        certificatesAdapter += CertificatesAdapterDelegate(
            onItemClick = { screenManager.showPdfInBrowserByGoogleDocs(this, it) },
            onShareButtonClick = { onNeedShowShareDialog(it) }
        )

        with(certificateRecyclerView) {
            adapter = certificatesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager)
                        ?: return

                    if (dy > 0) {
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            post {
                                certificatesPresenter.fetchNextPageFromRemote(userId)
                            }
                        }
                    }
                }
            })
        }

        initViewStateDelegate()

        certificateSwipeRefresh.setOnRefreshListener { certificatesPresenter.forceUpdate(userId) }
        tryAgain.setOnClickListener { certificatesPresenter.forceUpdate(userId) }
        goToCatalog.setOnClickListener { screenManager.showCatalog(this) }

        certificatesPresenter.fetchCertificates(userId)
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
        viewStateDelegate.addState<CertificatesView.State.NetworkError>(error)
        viewStateDelegate.addState<CertificatesView.State.CertificatesCache>(certificateSwipeRefresh, certificateRecyclerView)
        viewStateDelegate.addState<CertificatesView.State.CertificatesRemote>(certificateSwipeRefresh, certificateRecyclerView)
        viewStateDelegate.addState<CertificatesView.State.CertificatesRemoteLoading>(certificateSwipeRefresh, certificateRecyclerView, loadProgressbarOnEmptyScreen)
    }

    override fun setState(state: CertificatesView.State) {
        certificateSwipeRefresh.isRefreshing = false
        certificateSwipeRefresh.isEnabled = (state is CertificatesView.State.CertificatesRemote ||
                state is CertificatesView.State.CertificatesCache ||
                state is CertificatesView.State.NetworkError)
        viewStateDelegate.switchState(state)
        when (state) {
            is CertificatesView.State.CertificatesCache ->
                certificatesAdapter.items = state.certificates
            is CertificatesView.State.CertificatesRemote ->
                certificatesAdapter.items = state.certificates
            is CertificatesView.State.CertificatesRemoteLoading ->
                certificatesAdapter.items = state.certificates
        }
    }

    override fun showNetworkError() {
        Snackbar
            .make(root, R.string.connectionProblems, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
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