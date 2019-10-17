package org.stepik.android.view.download.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_download.*
import kotlinx.android.synthetic.main.empty_certificates.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.model.Course
import org.stepik.android.presentation.download.DownloadPresenter
import org.stepik.android.presentation.download.DownloadView
import org.stepik.android.view.download.ui.adapter.DownloadedCoursesAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import javax.inject.Inject

class DownloadActivity : FragmentActivityBase(), DownloadView {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, DownloadActivity::class.java)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var downloadPresenter: DownloadPresenter

    private val downloadedCoursesAdapter: DefaultDelegateAdapter<DownloadItem> = DefaultDelegateAdapter()

    private val viewStateDelegate =
        ViewStateDelegate<DownloadView.State>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        injectComponent()
        downloadPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(DownloadPresenter::class.java)

        initCenteredToolbar(R.string.downloads_title, showHomeButton = true)

        downloadedCoursesAdapter += DownloadedCoursesAdapterDelegate { screenManager.showCourseModules(this, it.course) }

        with(downloadsRecyclerView) {
            adapter = downloadedCoursesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@DownloadActivity, LinearLayoutManager.VERTICAL))
        }

        initViewStateDelegate()
        goToCatalog.setOnClickListener { screenManager.showCatalog(this) }
        downloadPresenter.fetchDownloadedCourses()
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

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<DownloadView.State.Idle>()
        viewStateDelegate.addState<DownloadView.State.Loading>(loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<DownloadView.State.DownloadedCoursesLoaded>(downloadsRecyclerView)
    }

    override fun setState(state: DownloadView.State) {
        viewStateDelegate.switchState(state)
        if (state is DownloadView.State.DownloadedCoursesLoaded) {
            downloadedCoursesAdapter.items = state.courses
        }
    }
}