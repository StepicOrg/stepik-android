package org.stepik.android.view.downloads.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.empty_certificates.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar

class DownloadActivity : FragmentActivityBase() {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, DownloadActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        initCenteredToolbar(R.string.downloads_title, showHomeButton = true)

        goToCatalog.setOnClickListener { screenManager.showCatalog(this) }
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