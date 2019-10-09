package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.TagListPresenter
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.model.CourseListType
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.argument
import org.stepik.android.model.Tag
import javax.inject.Inject

class TagFragment : CourseListFragmentBase(), CoursesView {

    companion object {
        fun newInstance(tag: Tag): Fragment =
            TagFragment()
                .apply {
                    this.tag = tag
                }
    }

    private var tag: Tag by argument()

    @Inject
    lateinit var tagListPresenter: TagListPresenter

    override fun injectComponent() {
        App
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .tagComponentBuilder()
                .tag(tag)
                .build()
                .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = tag.title
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

    override fun getCourseType(): CourseListType? = null

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
