package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.CourseCollectionPresenter
import org.stepic.droid.model.CollectionDescriptionColors
import org.stepic.droid.model.CoursesDescriptionContainer
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.util.initCenteredToolbar
import javax.inject.Inject

class CourseCollectionFragment : CourseListFragmentBase() {
    companion object {
        private const val TITLE_KEY = "title_key"
        private const val COURSE_IDS = "course_ids"
        private const val DESCRIPTION_TEXT = "description_text"
        private const val DESCRIPTION_COLORS = "description_colors"

        fun newInstance(title: String, courseIds: LongArray, descriptionText: String, descriptionColors: CollectionDescriptionColors?): CourseCollectionFragment {
            val args = Bundle().apply {
                putString(TITLE_KEY, title)
                putLongArray(COURSE_IDS, courseIds)
                putString(DESCRIPTION_TEXT, descriptionText)
                putSerializable(DESCRIPTION_COLORS, descriptionColors)
            }
            return CourseCollectionFragment().apply { arguments = args }
        }
    }

    @Inject
    lateinit var courseCollectionPresenter: CourseCollectionPresenter

    override fun injectComponent() {
        App
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(getTitle())
        courseCollectionPresenter.attachView(this)
        courseCollectionPresenter.onShowCollections(arguments.getLongArray(COURSE_IDS))

        val descriptionText = arguments.getString(DESCRIPTION_TEXT)
        val descriptionColors = arguments.getSerializable(DESCRIPTION_COLORS) as CollectionDescriptionColors?

        descriptionColors?.let {
            coursesAdapter.setDescriptionContainer(CoursesDescriptionContainer(descriptionText, it))
        }
    }

    override fun onDestroyView() {
        courseCollectionPresenter.detachView(this)
        super.onDestroyView()
    }

    override fun onRefresh() {
        courseCollectionPresenter.onShowCollections(arguments.getLongArray(COURSE_IDS))
    }

    override fun getCourseType(): Table? = null

    override fun onNeedDownloadNextPage() {
        //no op
    }

    override fun showEmptyScreen(isShow: Boolean) {

    }

    fun getTitle(): String = arguments.getString(TITLE_KEY)
}
