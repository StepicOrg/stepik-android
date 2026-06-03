package org.stepik.android.view.certificate.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.databinding.ActivityCertificatesBinding
import org.stepic.droid.model.CertificateListItem
import org.stepic.droid.ui.dialogs.CertificateShareDialogFragment
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.model.Certificate
import org.stepik.android.presentation.certificate.CertificatesPresenter
import org.stepik.android.presentation.certificate.CertificatesView
import org.stepik.android.view.certificate.ui.adapter.delegate.CertificatesAdapterDelegate
import org.stepik.android.view.certificate.ui.dialog.CertificateNameChangeConfirmationDialog
import org.stepik.android.view.certificate.ui.dialog.CertificateNameChangeDialog
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.core.model.PaginationDirection
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class CertificatesActivity :
    FragmentActivityBase(),
    CertificatesView,
    CertificateNameChangeDialog.Callback,
    CertificateNameChangeConfirmationDialog.Callback {
    companion object {
        private const val EXTRA_USER_ID = "user_id"
        private const val EXTRA_IS_CURRENT_USER = "is_current_user"

        fun createIntent(context: Context, userId: Long, isCurrentUser: Boolean): Intent =
            Intent(context, CertificatesActivity::class.java)
                .putExtra(EXTRA_USER_ID, userId)
                .putExtra(EXTRA_IS_CURRENT_USER, isCurrentUser)
    }

    private var userId: Long = -1
    private var isCurrentUser: Boolean = false

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val certificatesPresenter: CertificatesPresenter by viewModels { viewModelFactory }

    private var certificatesAdapter: DefaultDelegateAdapter<CertificateListItem> = DefaultDelegateAdapter()

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private val viewStateDelegate =
        ViewStateDelegate<CertificatesView.State>()

    private val binding: ActivityCertificatesBinding by viewBinding(ActivityCertificatesBinding::bind)

    // Views from included layouts — not in ActivityCertificatesBinding
    private lateinit var tryAgainView: View
    private lateinit var goToCatalogView: View
    private lateinit var reportEmptyCertificatesView: View
    private lateinit var loadProgressbarOnEmptyScreenView: View
    private lateinit var errorView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificates)

        injectComponent()

        initCenteredToolbar(R.string.certificates_title, showHomeButton = true)
        userId = intent.getLongExtra(EXTRA_USER_ID, -1)
        isCurrentUser = intent.getBooleanExtra(EXTRA_IS_CURRENT_USER, false)

        // Views from included layouts — not in ActivityCertificatesBinding
        tryAgainView = findViewById(R.id.tryAgain)
        goToCatalogView = findViewById(R.id.goToCatalog)
        reportEmptyCertificatesView = findViewById(R.id.reportEmptyCertificates)
        loadProgressbarOnEmptyScreenView = findViewById(R.id.loadProgressbarOnEmptyScreen)
        errorView = findViewById(R.id.error)

        certificatesAdapter += CertificatesAdapterDelegate(
            onItemClick = { screenManager.showPdfInBrowserByGoogleDocs(this, it) },
            onShareButtonClick = { onNeedShowShareDialog(it) },
            onChangeNameClick = { showChangeNameDialog(certificate = it.certificate) },
            isCurrentUser = isCurrentUser
        )

        with(binding.certificateRecyclerView) {
            adapter = certificatesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            setOnPaginationListener { paginationDirection ->
                if (paginationDirection == PaginationDirection.NEXT) {
                    certificatesPresenter.fetchNextPageFromRemote(userId)
                }
            }
        }

        initViewStateDelegate()

        binding.certificateSwipeRefresh.setOnRefreshListener { certificatesPresenter.forceUpdate(userId) }
        tryAgainView.setOnClickListener { certificatesPresenter.forceUpdate(userId) }
        goToCatalogView.setOnClickListener { screenManager.showCatalog(this) }

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
        viewStateDelegate.addState<CertificatesView.State.EmptyCertificates>(reportEmptyCertificatesView)
        viewStateDelegate.addState<CertificatesView.State.Loading>(loadProgressbarOnEmptyScreenView)
        viewStateDelegate.addState<CertificatesView.State.NetworkError>(errorView)
        viewStateDelegate.addState<CertificatesView.State.CertificatesCache>(binding.certificateSwipeRefresh, binding.certificateRecyclerView)
        viewStateDelegate.addState<CertificatesView.State.CertificatesRemote>(binding.certificateSwipeRefresh, binding.certificateRecyclerView)
        viewStateDelegate.addState<CertificatesView.State.CertificatesRemoteLoading>(binding.certificateSwipeRefresh, binding.certificateRecyclerView, loadProgressbarOnEmptyScreenView)
    }

    override fun setState(state: CertificatesView.State) {
        binding.certificateSwipeRefresh.isRefreshing = false
        binding.certificateSwipeRefresh.isEnabled = (state is CertificatesView.State.CertificatesRemote ||
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
            else -> Unit
        }
    }

    override fun showNetworkError() {
        binding.root.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun showChangeNameSuccess() {
        binding.root.snackbar(messageRes = R.string.certificate_name_change_snackbar_success)
    }

    override fun showChangeNameDialogError(certificate: Certificate, attemptedFullName: String) {
        showChangeNameDialog(certificate, attemptedFullName)
    }

    private fun onNeedShowShareDialog(certificateListItem: CertificateListItem.Data?) {
        if (certificateListItem == null) {
            return
        }
        CertificateShareDialogFragment
            .newInstance(certificateListItem)
            .showIfNotExists(supportFragmentManager, CertificateShareDialogFragment.TAG)
    }

    override fun showUpdateCertificateConfirmationDialog(newFullName: String, certificate: Certificate) {
        CertificateNameChangeConfirmationDialog
            .newInstance(newFullName, certificate)
            .showIfNotExists(supportFragmentManager, CertificateNameChangeConfirmationDialog.TAG)
    }

    override fun updateCertificate(certificate: Certificate, newFullName: String) {
        certificatesPresenter.updateCertificate(certificate, newFullName)
    }

    private fun showChangeNameDialog(certificate: Certificate, attemptedFullName: String = "") {
        CertificateNameChangeDialog
            .newInstance(certificate, attemptedFullName)
            .showIfNotExists(supportFragmentManager, CertificateNameChangeDialog.TAG)
    }
}
