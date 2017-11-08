package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.CourseListCollection
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.util.initCenteredToolbar
import javax.inject.Inject

class CourseCollectionFragment : CourseListFragmentBase() {
    companion object {
        private const val TITLE_KEY = "title_key"
        private const val COURSE_IDS = "course_ids"

        fun newInstance(title: String, courseIds: LongArray): CourseCollectionFragment {
            val args = Bundle().apply {
                putString(TITLE_KEY, title)
                putLongArray(COURSE_IDS, courseIds)
            }
            return CourseCollectionFragment().apply { arguments = args }
        }
    }

    @Inject
    lateinit var courseListCollection: CourseListCollection

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
        courseListCollection.attachView(this)
        courseListCollection.onShowCollection(arguments.getLongArray(COURSE_IDS))
    }

    override fun onDestroy() {
        super.onDestroy()
        courseListCollection.detachView(this)
    }

    override fun onRefresh() {
        courseListCollection.onShowCollection(arguments.getLongArray(COURSE_IDS))
    }

    override fun getCourseType(): Table? = null

    override fun onNeedDownloadNextPage() {
        //no op
    }

    override fun showEmptyScreen(isShow: Boolean) {

    }

    fun getTitle(): String = arguments.getString(TITLE_KEY)
}
