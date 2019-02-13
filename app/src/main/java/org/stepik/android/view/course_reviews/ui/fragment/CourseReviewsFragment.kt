package org.stepik.android.view.course_reviews.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_course_reviews.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.util.argument
import org.stepik.android.view.course_reviews.ui.adapter.CourseReviewsAdapter
import javax.inject.Inject

class CourseReviewsFragment : Fragment() {
    companion object {
        fun newInstance(courseId: Long): Fragment =
            CourseReviewsFragment().apply {
                this.courseId = courseId
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var courseId: Long by argument()

    private lateinit var courseReviewsAdapter: CourseReviewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent(courseId)
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
        courseReviewsAdapter = CourseReviewsAdapter()

        with(courseReviewsRecycler) {
            layoutManager = LinearLayoutManager(context)
            adapter = courseReviewsAdapter

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.list_divider_h)?.let(::setDrawable)
            })
        }
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}