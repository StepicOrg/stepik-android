package org.stepik.android.view.course_list.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.presentation.course_list.CourseListView
import timber.log.Timber
import javax.inject.Inject

class CourseListActivity : FragmentActivityBase(), CourseListView {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListPresenter: CourseListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        injectComponent()

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListPresenter::class.java)

        courseListPresenter.fetchCourses(courseListQuery = CourseListQuery(page = 1, order = CourseListQuery.ORDER_ACTIVITY_DESC, teacher = 651763))
    }

    private fun injectComponent() {
        App.component()
            .courseListExperimentalComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        courseListPresenter.attachView(this)
    }

    override fun onStop() {
        courseListPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: CourseListView.State) {
        if (state is CourseListView.State.Content) {
            Timber.d("Page: ${state.courseListItems.page} Items: ${state.courseListItems.toList()}")
        }
    }

    override fun showNetworkError() {
        // TODO
    }
}