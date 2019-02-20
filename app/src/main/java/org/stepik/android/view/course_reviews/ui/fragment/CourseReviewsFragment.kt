package org.stepik.android.view.course_reviews.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.empty_default.*
import kotlinx.android.synthetic.main.error_no_connection.*
import kotlinx.android.synthetic.main.fragment_course_reviews.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.presentation.course_reviews.CourseReviewsPresenter
import org.stepik.android.presentation.course_reviews.CourseReviewsView
import org.stepik.android.view.course_reviews.ui.adapter.CourseReviewsAdapter
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseReviewsFragment : Fragment(), CourseReviewsView {
    companion object {
        fun newInstance(courseId: Long): Fragment =
            CourseReviewsFragment().apply {
                this.courseId = courseId
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    private var courseId: Long by argument()

    private lateinit var courseReviewsAdapter: CourseReviewsAdapter
    private lateinit var courseReviewsPresenter: CourseReviewsPresenter
    private lateinit var viewStateDelegate: ViewStateDelegate<CourseReviewsView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent(courseId)

        courseReviewsPresenter = ViewModelProviders.of(this, viewModelFactory).get(CourseReviewsPresenter::class.java)
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
        inflater.inflate(R.layout.fragment_course_reviews, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        courseReviewsAdapter = CourseReviewsAdapter { screenManager.openProfile(activity, it.id) }

        with(courseReviewsRecycler) {
            layoutManager = LinearLayoutManager(context)
            adapter = courseReviewsAdapter

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.list_divider_h)?.let(::setDrawable)
            })

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager)
                        ?: return

                    if (dy > 0) {
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            post {
                                courseReviewsPresenter.fetchNextPageFromRemote()
                            }
                        }
                    }
                }
            })
        }

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<CourseReviewsView.State.Idle>(courseReviewsPlaceholder)
        viewStateDelegate.addState<CourseReviewsView.State.Loading>(courseReviewsPlaceholder)
        viewStateDelegate.addState<CourseReviewsView.State.CourseReviewsCache>(courseReviewsRecycler)
        viewStateDelegate.addState<CourseReviewsView.State.CourseReviewsRemote>(courseReviewsRecycler)
        viewStateDelegate.addState<CourseReviewsView.State.CourseReviewsRemoteLoading>(courseReviewsRecycler)
        viewStateDelegate.addState<CourseReviewsView.State.NetworkError>(reportProblem)
        viewStateDelegate.addState<CourseReviewsView.State.EmptyContent>(report_empty)
    }

    override fun onStart() {
        super.onStart()
        courseReviewsPresenter.attachView(this)
    }

    override fun onStop() {
        courseReviewsPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: CourseReviewsView.State) {
        viewStateDelegate.switchState(state)
        when (state) {
            is CourseReviewsView.State.CourseReviewsCache ->
                courseReviewsAdapter.items = state.courseReviewItems

            is CourseReviewsView.State.CourseReviewsRemote ->
                courseReviewsAdapter.items = state.courseReviewItems

            is CourseReviewsView.State.CourseReviewsRemoteLoading ->
                courseReviewsAdapter.items = state.courseReviewItems + CourseReviewItem.Placeholder
        }
    }

    override fun showNetworkError() {
        val view = view
            ?: return

        Snackbar
            .make(view, R.string.connectionProblems, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(view.context, R.color.white))
            .show()
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}