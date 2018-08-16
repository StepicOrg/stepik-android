package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
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
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.hideAllChildren
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.model.Video
import javax.inject.Inject

class DownloadsFragment: FragmentBase(), DownloadsView {
    companion object {
        fun newInstance() = DownloadsFragment()

        private const val ANIMATION_DURATION = 10L
    }

    @Inject
    lateinit var downloadsPresenter: DownloadsPresenter

    private lateinit var downloadsAdapter: DownloadsAdapter

    override fun injectComponent() {
        App
                .componentManager()
                .downloadsComponent()
                .inject(this)

        setHasOptionsMenu(true)
        downloadsAdapter = DownloadsAdapter(downloadsPresenter)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_downloads, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        initCenteredToolbar(R.string.downloads, true)

        needAuthView.changeVisibility(false)
        authAction.setOnClickListener { screenManager.showLaunchScreen(context) }

        with(list_of_downloads) {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = SlideInRightAnimator()
            adapter = downloadsAdapter

            with(itemAnimator) {
                removeDuration = ANIMATION_DURATION
                addDuration = ANIMATION_DURATION
                moveDuration = ANIMATION_DURATION
            }
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
            if (item?.itemId == android.R.id.home) {
                activity?.onBackPressed()
                true
            } else {
                super.onOptionsItemSelected(item)
            }

    override fun showVideo(video: Video) {
        screenManager.showVideo(activity, video, null)
    }

    override fun onStart() {
        super.onStart()
        downloadsPresenter.attachView(this)
    }

    override fun onStop() {
        downloadsPresenter.detachView(this)
        super.onStop()
    }
}