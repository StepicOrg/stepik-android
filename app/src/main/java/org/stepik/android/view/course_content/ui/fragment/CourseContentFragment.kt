package org.stepik.android.view.course_content.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_course_content.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.view.course_content.ui.adapter.CourseContentAdapter
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitClickListener
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.util.argument
import org.stepik.android.presentation.course_content.CourseContentPresenter
import org.stepik.android.presentation.course_content.CourseContentView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseContentFragment : Fragment(), CourseContentView {
    companion object {
        fun newInstance(courseId: Long) =
                CourseContentFragment().apply {
                    this.courseId = courseId
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var contentAdapter: CourseContentAdapter
    private var courseId: Long by argument()

    private lateinit var courseContentPresenter: CourseContentPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<CourseContentView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent(courseId)

        courseContentPresenter = ViewModelProviders.of(this, viewModelFactory).get(CourseContentPresenter::class.java)
        savedInstanceState?.let(courseContentPresenter::onRestoreInstanceState)
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_course_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(courseContentRecycler) {
            contentAdapter =
                    CourseContentAdapter(object :
                        CourseContentUnitClickListener {
                        override fun onItemClicked(item: CourseContentItem.UnitItem) {}
                        override fun onItemDownloadClicked(item: CourseContentItem.UnitItem) {}
                        override fun onItemRemoveClicked(item: CourseContentItem.UnitItem) {}
                    })
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.list_divider_h)?.let(::setDrawable)
            })
        }

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<CourseContentView.State.Idle>(courseContentPlaceholder)
        viewStateDelegate.addState<CourseContentView.State.Loading>(courseContentPlaceholder)
        viewStateDelegate.addState<CourseContentView.State.CourseContentLoaded>(courseContentRecycler)
        viewStateDelegate.addState<CourseContentView.State.NetworkError>(error)
    }

    override fun onStart() {
        super.onStart()
        courseContentPresenter.attachView(this)
    }

    override fun onStop() {
        courseContentPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: CourseContentView.State) {
        viewStateDelegate.switchState(state)
        if (state is CourseContentView.State.CourseContentLoaded) {
            contentAdapter.setData(state.courseContent)
        }
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}
