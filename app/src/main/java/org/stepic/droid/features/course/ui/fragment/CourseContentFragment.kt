package org.stepic.droid.features.course.ui.fragment

import android.graphics.drawable.ColorDrawable
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
import org.stepic.droid.features.course.ui.adapter.course_content.CourseContentAdapter
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson.CourseContentLessonClickListener
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument
import org.stepik.android.model.*
import org.stepik.android.model.Unit

class CourseContentFragment : Fragment() {
    companion object {
        fun newInstance(course: Course) =
                CourseContentFragment().apply {
                    this.course = course
                }
    }

    private lateinit var contentAdapter: CourseContentAdapter
    private var course by argument<Course>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_course_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(courseContentRecycler) {
            contentAdapter = CourseContentAdapter(object : CourseContentLessonClickListener {
                override fun onItemClicked(item: CourseContentItem.LessonItem) {}
                override fun onItemDownloadClicked(item: CourseContentItem.LessonItem) {}
                override fun onItemRemoveClicked(item: CourseContentItem.LessonItem) {}
            })
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.course_content_separator)))
            })
        }

        contentAdapter.setData(listOf(
                CourseContentItem.SectionItem(
                        Section(title = "Introduction to JavaScript"),
                        Progress(nSteps = 70, nStepsPassed = 23),
                        DownloadProgress(0, DownloadProgress.Status.NotCached)
                ),
                CourseContentItem.LessonItem(
                        Section(title = "Introduction to JavaScript"),
                        Unit(),
                        Lesson(title = "First lesson with short name", coverUrl = "https://i.vimeocdn.com/video/568468317_180x180.jpg?r=pad"),
                        Progress(nSteps = 10, nStepsPassed = 8),
                        DownloadProgress(0, DownloadProgress.Status.NotCached)
                )
        ))
    }
}
