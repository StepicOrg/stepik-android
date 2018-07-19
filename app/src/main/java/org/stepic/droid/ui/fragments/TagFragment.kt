package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.TagListPresenter
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.model.Tag
import javax.inject.Inject

class TagFragment : CourseListFragmentBase(), CoursesView {

    companion object {

        private const val TAG_KEY = "tag_key"

        fun newInstance(tag: Tag): TagFragment {
            val bundle = Bundle().apply { putParcelable(TAG_KEY, tag) }
            return TagFragment().apply { arguments = bundle }
        }
    }

    @Inject
    lateinit var tagListPresenter: TagListPresenter

    override fun injectComponent() {
        App
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .tagComponentBuilder()
                .tag(arguments.getParcelable<Tag>(TAG_KEY))
                .build()
                .inject(this)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments.getParcelable<Tag>(TAG_KEY).title
        initCenteredToolbar(title, showHomeButton = true, homeIndicatorRes = getCloseIconDrawableRes())

        tagListPresenter.attachView(this)
        tagListPresenter.onInitTag()
        tagListPresenter.downloadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tagListPresenter.detachView(this)
    }

    override fun onRefresh() {
        tagListPresenter.refreshData()
    }

    override fun getCourseType(): Table? = null

    override fun onNeedDownloadNextPage() {
        tagListPresenter.downloadData()
    }

    override fun showEmptyScreen(isShown: Boolean) {
        if (isShown) {
            emptySearch.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE
        } else {
            emptySearch.visibility = View.GONE
            swipeRefreshLayout.visibility = View.VISIBLE
        }
    }
}
