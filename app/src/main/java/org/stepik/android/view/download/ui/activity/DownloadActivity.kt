package org.stepik.android.view.download.ui.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.activity_download.*
import kotlinx.android.synthetic.main.empty_certificates.goToCatalog
import kotlinx.android.synthetic.main.empty_downloading.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.TextUtil
import org.stepik.android.model.Course
import org.stepik.android.presentation.download.DownloadPresenter
import org.stepik.android.presentation.download.DownloadView
import org.stepik.android.view.course_content.ui.dialog.RemoveCachedContentDialog
import org.stepik.android.view.download.ui.adapter.DownloadedCoursesAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class DownloadActivity : FragmentActivityBase(), DownloadView, RemoveCachedContentDialog.Callback {
    companion object {
        private const val MB = 1024 * 1024L

        fun createIntent(context: Context): Intent =
            Intent(context, DownloadActivity::class.java)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var downloadPresenter: DownloadPresenter

    private val downloadedCoursesAdapter: DefaultDelegateAdapter<DownloadItem> = DefaultDelegateAdapter()

    private val viewStateDelegate =
        ViewStateDelegate<DownloadView.State>()

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        injectComponent()
        downloadPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(DownloadPresenter::class.java)

        initCenteredToolbar(R.string.downloads_title, showHomeButton = true)

        downloadedCoursesAdapter += DownloadedCoursesAdapterDelegate(
            onItemClick = { screenManager.showCourseModules(this, it.course) },
            onItemRemoveClick = ::showRemoveCourseDialog
        )

        with(downloadsRecyclerView) {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = downloadedCoursesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@DownloadActivity, LinearLayoutManager.VERTICAL))
        }

        initViewStateDelegate()
        goToCatalog.setOnClickListener { screenManager.showCatalog(this) }
        downloadPresenter.fetchStorage()
        downloadPresenter.fetchDownloadedCourses()

        downloadsOtherApps.supportCompoundDrawablesTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.custom_grey))
        downloadsStepik.supportCompoundDrawablesTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green01))
        downloadsFree.supportCompoundDrawablesTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey04))
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
        viewStateDelegate.addState<DownloadView.State.Empty>(emptyDownloading)
        viewStateDelegate.addState<DownloadView.State.DownloadedCoursesLoaded>(downloadStorageContainer, downloadsRecyclerView)
    }

    override fun setState(state: DownloadView.State) {
        viewStateDelegate.switchState(state)
        if (state is DownloadView.State.DownloadedCoursesLoaded) {
            downloadedCoursesAdapter.items = state.courses
        }
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun setStorageInfo(contentSize: Long, avalableSize: Long, totalSize: Long) {
        downloadStorageUsed.text = buildSpannedString {
            bold { append(TextUtil.formatBytes(contentSize, MB)) }
            append(resources.getString(R.string.downloads_is_used_by_stepik))
        }
        downloadsFree.text = resources.getString(R.string.downloads_free_space, TextUtil.formatBytes(avalableSize, MB))
        downloadsStorageProgress.max = (totalSize / MB).toInt()
        downloadsStorageProgress.progress = ((totalSize - avalableSize) / MB).toInt()
        downloadsStorageProgress.secondaryProgress = (downloadsStorageProgress.progress + (contentSize / MB)).toInt()
    }

    private fun showRemoveCourseDialog(downloadItem: DownloadItem) {
        RemoveCachedContentDialog
            .newInstance(course = downloadItem.course)
            .showIfNotExists(supportFragmentManager, RemoveCachedContentDialog.TAG)
    }

    override fun onRemoveCourseDownloadConfirmed(course: Course) {
        downloadPresenter.removeCourseDownload(course)
    }

    override fun showRemoveTaskError() {
        root.snackbar(messageRes = R.string.downloads_remove_task_error)
    }
}