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
import kotlinx.android.synthetic.main.fragment_course_content.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.adapter.course_content.CourseContentAdapter
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.unit.CourseContentUnitClickListener
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.persistence.model.DownloadProgress
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
            contentAdapter = CourseContentAdapter(object : CourseContentUnitClickListener {
                override fun onItemClicked(item: CourseContentItem.UnitItem) {}
                override fun onItemDownloadClicked(item: CourseContentItem.UnitItem) {}
                override fun onItemRemoveClicked(item: CourseContentItem.UnitItem) {}
            })
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.course_content_separator)))
            })
        }

        contentAdapter.setData(listOf(
                CourseContentItem.SectionItem(
                        Section(title = "Introduction to JavaScript", position = 1),
                        Progress(nSteps = 70, nStepsPassed = 23),
                        DownloadProgress.Status.Cached
                ),
                *Array(5) {
                    CourseContentItem.UnitItem(
                            Section(title = "Introduction to JavaScript", position = 1),
                            Unit(position = it + 1),
                            Lesson(title = "First lesson with short name", coverUrl = "https://i.vimeocdn.com/video/507126040_180x180.jpg"),
                            Progress(nSteps = 10, nStepsPassed = 8),
                            DownloadProgress.Status.InProgress(0.43f)
                    )
                }
        ))
    }
}
