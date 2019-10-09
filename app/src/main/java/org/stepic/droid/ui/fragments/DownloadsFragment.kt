package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.empty_downloading.*
import kotlinx.android.synthetic.main.empty_login.*
import kotlinx.android.synthetic.main.fragment_downloads.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.DownloadsPresenter
import org.stepic.droid.core.presenters.contracts.DownloadsView
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.ui.adapters.DownloadsAdapter
import org.stepic.droid.ui.dialogs.CancelVideosDialog
import org.stepic.droid.ui.dialogs.ClearVideosDialog
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.hideAllChildren
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class DownloadsFragment: FragmentBase(), DownloadsView {
    companion object {
        fun newInstance() = DownloadsFragment()
    }

    @Inject
    lateinit var downloadsPresenter: DownloadsPresenter

    private lateinit var downloadsAdapter: DownloadsAdapter

    private val loadingProgressDialog by lazy {
        LoadingProgressDialogFragment.newInstance()
    }

    override fun injectComponent() {
        App
            .componentManager()
            .downloadsComponent()
            .inject(this)

        setHasOptionsMenu(true)
        downloadsAdapter = DownloadsAdapter(downloadsPresenter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_downloads, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        initCenteredToolbar(R.string.downloads, true)

        needAuthView.changeVisibility(false)
        authAction.setOnClickListener { screenManager.showLaunchScreen(context) }

        with(list_of_downloads) {
            layoutManager = LinearLayoutManager(context)
            adapter = downloadsAdapter
        }

        goToCatalog.setOnClickListener { screenManager.showCatalog(context) }
        container.hideAllChildren()
    }

    override fun addActiveDownload(downloadItem: DownloadItem) =
            downloadsAdapter.addActiveDownload(downloadItem)

    override fun addCompletedDownload(downloadItem: DownloadItem) =
            downloadsAdapter.addCompletedDownload(downloadItem)

    override fun removeDownload(downloadItem: DownloadItem) =
            downloadsAdapter.removeDownload(downloadItem)

    override fun showEmptyAuth() {
        needAuthView.changeVisibility(true)
    }

    override fun invalidateEmptyDownloads() {
        if (downloadsAdapter.itemCount == 0) {
            if (empty_downloading.visibility != View.VISIBLE) {
                container.hideAllChildren()
                empty_downloading.changeVisibility(true)
            }
        } else {
            if (list_of_downloads.visibility != View.VISIBLE) {
                container.hideAllChildren()
                list_of_downloads.changeVisibility(true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun showVideo(videoPlayerMediaData: VideoPlayerMediaData) {
        screenManager.showVideo(this, videoPlayerMediaData, false)
    }

    override fun onStart() {
        super.onStart()
        downloadsPresenter.attachView(this)
    }

    override fun onStop() {
        downloadsPresenter.detachView(this)
        super.onStop()
    }

    override fun askToCancelAllVideos() {
        val dialog = CancelVideosDialog.newInstance()
        dialog.setTargetFragment(this@DownloadsFragment, CancelVideosDialog.REQUEST_CODE)
        dialog.show(requireFragmentManager(), CancelVideosDialog.TAG)
    }

    override fun askToRemoveAllCachedVideos() {
        val dialog = ClearVideosDialog.newInstance()
        dialog.setTargetFragment(this@DownloadsFragment, ClearVideosDialog.REQUEST_CODE)
        dialog.show(requireFragmentManager(), ClearVideosDialog.TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when {
        requestCode == CancelVideosDialog.REQUEST_CODE && resultCode == Activity.RESULT_OK ->
            downloadsPresenter.removeDownloads(downloadsAdapter.activeDownloads)

        requestCode == ClearVideosDialog.REQUEST_CODE && resultCode == Activity.RESULT_OK ->
            downloadsPresenter.removeDownloads(downloadsAdapter.completedDownloads)

        else ->
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showLoading() {
        ProgressHelper.activate(loadingProgressDialog, fragmentManager, LoadingProgressDialogFragment.TAG)
    }

    override fun hideLoading() {
        ProgressHelper.dismiss(fragmentManager, LoadingProgressDialogFragment.TAG)
    }

    override fun onCantRemoveVideo() {
        view?.snackbar(messageRes = R.string.downloads_view_cant_remove)
    }
}