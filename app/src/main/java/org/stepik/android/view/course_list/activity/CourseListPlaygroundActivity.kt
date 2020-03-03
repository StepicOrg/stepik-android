package org.stepik.android.view.course_list.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.presentation.course_list.CourseListView
import javax.inject.Inject

class CourseListPlaygroundActivity : FragmentActivityBase(), CourseListView {

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

        courseListPresenter.getCourseListItems(4261L, 2L, 79L, 2852L, 15001L)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}