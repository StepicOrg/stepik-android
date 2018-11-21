package org.stepik.android.view.course_info.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.error_course_not_found.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_course_info.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.view.course_info.ui.adapter.CourseInfoAdapter
import org.stepik.android.view.course_info.ui.adapter.decorators.CourseInfoBlockOffsetDecorator
import org.stepik.android.view.course_info.ui.adapter.delegates.CourseInfoInstructorsDelegate
import org.stepik.android.view.course_info.ui.adapter.delegates.CourseInfoTextBlockDelegate
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument
import org.stepik.android.presentation.course_info.CourseInfoPresenter
import org.stepik.android.presentation.course_info.CourseInfoView
import org.stepik.android.view.course_info.mapper.toSortedItems
import javax.inject.Inject

class CourseInfoFragment : Fragment(), CourseInfoView {
    companion object {
        fun newInstance(courseId: Long): Fragment =
                CourseInfoFragment().apply {
                    this.courseId = courseId
                }
    }

    private var courseId: Long by argument()
    private val adapter = CourseInfoAdapter()

    private lateinit var courseInfoPresenter: CourseInfoPresenter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent(courseId)

        courseInfoPresenter = ViewModelProviders.of(this, viewModelFactory).get(CourseInfoPresenter::class.java)
        savedInstanceState?.let(courseInfoPresenter::onRestoreInstanceState)
    }

    private fun injectComponent(courseId: Long) {
        App.componentManager()
            .courseComponent(courseId)
            .inject(this)
    }

    private fun releaseComponent(courseId: Long) {
        App.componentManager()
            .releaseCourseComponent(courseId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_course_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        course_not_found.changeVisibility(false)
        error.changeVisibility(false)

        courseInfoRecycler.layoutManager = LinearLayoutManager(context)
        courseInfoRecycler.adapter = adapter

        courseInfoRecycler.addItemDecoration(
                CourseInfoBlockOffsetDecorator(resources.getDimension(R.dimen.course_info_block_margin).toInt(), intArrayOf(
                        adapter.delegates.indexOfFirst { it is CourseInfoTextBlockDelegate },
                        adapter.delegates.indexOfFirst { it is CourseInfoInstructorsDelegate }
                )))

    }

    override fun onStart() {
        super.onStart()
        courseInfoPresenter.attachView(this)
    }

    override fun onStop() {
        courseInfoPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: CourseInfoView.State) {
        if (state is CourseInfoView.State.CourseInfoLoaded) {
            adapter.setSortedData(state.courseInfoData.toSortedItems(requireContext()))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        courseInfoPresenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}