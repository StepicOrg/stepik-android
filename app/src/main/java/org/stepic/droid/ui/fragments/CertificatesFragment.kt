package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.empty_certificates.*
import kotlinx.android.synthetic.main.fragment_certificates.*
import kotlinx.android.synthetic.main.empty_login.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.error_no_connection.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.CertificatePresenter
import org.stepic.droid.core.presenters.contracts.CertificateView
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.ui.adapters.CertificatesAdapter
import org.stepic.droid.ui.dialogs.CertificateShareDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.ProgressHelper
import javax.inject.Inject

class CertificatesFragment : FragmentBase(),
        CertificateView,
        SwipeRefreshLayout.OnRefreshListener {
    companion object {
        fun newInstance(): Fragment = CertificatesFragment()
    }

    private var adapter: CertificatesAdapter? = null

    private val oldCoverColor: Int by lazy {
        ColorUtil.getColorArgb(R.color.old_cover, requireContext())
    }

    private val newCoverColor: Int by lazy {
        ColorUtil.getColorArgb(R.color.new_cover, requireContext())
    }

    @Inject
    lateinit var certificatePresenter: CertificatePresenter

    override fun injectComponent() {
        App
                .component()
                .certificateComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_certificates, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(R.string.certificates_title, false)

        adapter = CertificatesAdapter(certificatePresenter, requireActivity())
        certificateRecyclerView.layoutManager = LinearLayoutManager(context)
        certificateRecyclerView.adapter = adapter

        authAction.setOnClickListener { screenManager.showLaunchScreen(activity) }

        goToCatalog.setOnClickListener { screenManager.showCatalog(activity) }

        certificateSwipeRefresh.setOnRefreshListener(this)

        certificatePresenter.attachView(this)

        loadAndShowCertificates()
    }

    override fun onDestroyView() {
        certificatePresenter.detachView(this)
        authAction.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun loadAndShowCertificates() {
        certificatePresenter.showCertificates(false)
    }

    override fun onLoading() {
        if (certificatePresenter.size() <= 0) {
            certificateSwipeRefresh.visibility = View.GONE
            reportProblem.visibility = View.GONE
            reportEmptyCertificates.visibility = View.GONE
            certificateRootView.setBackgroundColor(newCoverColor)
            ProgressHelper.activate(loadProgressbarOnEmptyScreen)
        }
    }

    override fun showEmptyState() {
        needAuthView.visibility = View.GONE
        ProgressHelper.dismiss(certificateSwipeRefresh)
        ProgressHelper.dismiss(loadProgressbarOnEmptyScreen)
        reportProblem.visibility = View.GONE
        if (certificatePresenter.size() <= 0) {
            certificateRootView.setBackgroundColor(oldCoverColor)
            certificateSwipeRefresh.visibility = View.GONE
            reportEmptyCertificates.visibility = View.VISIBLE
        }
    }

    override fun onInternetProblem() {
        ProgressHelper.dismiss(certificateSwipeRefresh)
        ProgressHelper.dismiss(loadProgressbarOnEmptyScreen)
        reportEmptyCertificates.visibility = View.GONE
        needAuthView.visibility = View.GONE
        certificateSwipeRefresh.visibility = View.GONE
        if (certificatePresenter.size() <= 0) {
            certificateRootView.setBackgroundColor(oldCoverColor)
            reportProblem.visibility = View.VISIBLE
        } else {
            certificateSwipeRefresh.visibility = View.VISIBLE
        }
    }

    override fun onDataLoaded(certificateViewItems: List<CertificateViewItem>) {
        ProgressHelper.dismiss(loadProgressbarOnEmptyScreen)
        ProgressHelper.dismiss(certificateSwipeRefresh)
        reportEmptyCertificates.visibility = View.GONE
        reportProblem.visibility = View.GONE
        needAuthView.visibility = View.GONE
        certificateSwipeRefresh.visibility = View.VISIBLE
        certificateRecyclerView.visibility = View.VISIBLE
        certificateRootView.setBackgroundColor(oldCoverColor)
        adapter?.updateCertificates(certificateViewItems)
    }

    override fun onNeedShowShareDialog(certificateViewItem: CertificateViewItem?) {
        if (certificateViewItem == null) {
            return
        }
        val bottomSheetDialogFragment = CertificateShareDialogFragment.newInstance(certificateViewItem)
        if (!bottomSheetDialogFragment.isAdded) {
            bottomSheetDialogFragment.show(fragmentManager, null)
        }
    }

    override fun onAnonymousUser() {
        ProgressHelper.dismiss(certificateSwipeRefresh)
        ProgressHelper.dismiss(loadProgressbarOnEmptyScreen)
        reportEmptyCertificates.visibility = View.GONE
        reportProblem.visibility = View.GONE
        certificateRecyclerView.visibility = View.GONE
        certificateSwipeRefresh.visibility = View.GONE
        needAuthView.visibility = View.VISIBLE
        certificateRootView.setBackgroundColor(newCoverColor)
    }

    override fun onRefresh() {
        certificatePresenter.showCertificates(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
            when (item?.itemId) {
                android.R.id.home -> {
                    activity?.finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}
