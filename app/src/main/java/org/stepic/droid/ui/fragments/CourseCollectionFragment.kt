package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.CourseCollectionPresenter
import org.stepic.droid.model.CoursesDescriptionContainer
import org.stepic.droid.storage.structure.DbStructureCourseList
import org.stepic.droid.ui.util.initCenteredToolbar
import javax.inject.Inject

class CourseCollectionFragment : CourseListFragmentBase() {
    companion object {
        private const val TITLE_KEY = "title_key"
        private const val COURSE_IDS = "course_ids"
        private const val DESCRIPTION_CONTAINER = "description_container"

        fun newInstance(title: String, courseIds: LongArray, descriptionContainer: CoursesDescriptionContainer?): CourseCollectionFragment {
            val args = Bundle().apply {
                putString(TITLE_KEY, title)
                putLongArray(COURSE_IDS, courseIds)
                putParcelable(DESCRIPTION_CONTAINER, descriptionContainer)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(getTitle())
        courseCollectionPresenter.attachView(this)
        courseCollectionPresenter.onShowCollections(arguments?.getLongArray(COURSE_IDS) ?: longArrayOf())

        val descriptionContainer = arguments?.getParcelable<CoursesDescriptionContainer?>(DESCRIPTION_CONTAINER)

        descriptionContainer?.let {
            coursesAdapter.setDescriptionContainer(it)
        }
    }

    override fun onDestroyView() {
        courseCollectionPresenter.detachView(this)
        super.onDestroyView()
    }

    override fun onRefresh() {
        courseCollectionPresenter.onShowCollections(arguments?.getLongArray(COURSE_IDS) ?: longArrayOf())
    }

    override fun getCourseType(): DbStructureCourseList.Type? = null

    override fun onNeedDownloadNextPage() {
        //no op
    }

    override fun showEmptyScreen(isShown: Boolean) {

    }

    fun getTitle(): String = arguments?.getString(TITLE_KEY) ?: ""
}
