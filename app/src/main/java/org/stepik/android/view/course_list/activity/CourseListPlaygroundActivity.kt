package org.stepik.android.view.course_list.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepik.android.presentation.course_list.CourseListPlaygroundPresenter
import org.stepik.android.presentation.course_list.CourseListPlaygroundView
import org.stepik.android.presentation.course_list.CourseListView
import timber.log.Timber
import javax.inject.Inject

class CourseListPlaygroundActivity : FragmentActivityBase(), CourseListPlaygroundView {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListPlaygroundPresenter: CourseListPlaygroundPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        injectComponent()

        courseListPlaygroundPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListPlaygroundPresenter::class.java)

        courseListPlaygroundPresenter.fetchCourses(4261L, 2L, 79L, 2852L, 15001L)
        // courseListPlaygroundPresenter.onCourseIds(4261L, 2L, 79L, 2852L, 15001L)
    }

    private fun injectComponent() {
        App.component()
            .courseListExperimentalComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        courseListPlaygroundPresenter.attachView(this)
    }

    override fun onStop() {
        courseListPlaygroundPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: CourseListView.State) {
        Timber.d("State: $state")
    }
}