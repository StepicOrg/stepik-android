package org.stepik.android.view.download.ui

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.empty_certificates.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.download.DownloadPresenter
import org.stepik.android.presentation.download.DownloadView
import javax.inject.Inject

class DownloadActivity : FragmentActivityBase(), DownloadView {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, DownloadActivity::class.java)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var downloadPresenter: DownloadPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        injectComponent()
        downloadPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(DownloadPresenter::class.java)

        initCenteredToolbar(R.string.downloads_title, showHomeButton = true)

        goToCatalog.setOnClickListener { screenManager.showCatalog(this) }
    }

    private fun injectComponent() {
        App.component()
            .downloadComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        downloadPresenter.attachView(this)
    }

    override fun onStop() {
        downloadPresenter.detachView(this)
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
}