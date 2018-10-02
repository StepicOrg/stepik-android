package org.stepic.droid.features.course.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.error_course_not_found.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_course_info.*
import kotlinx.android.synthetic.main.view_course_info_organization.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.adapter.course_info.CourseInfoBlockAdapter
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoInstructorsBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoTextBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoType
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument
import org.stepik.android.model.Course
import org.stepik.android.model.user.User

class CourseInfoFragment : Fragment() {
    companion object {
        fun newInstance(course: Course): Fragment =
                CourseInfoFragment().apply {
                    this.course = course
                }
    }

    private var course by argument<Course>()

    private val adapter = CourseInfoBlockAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_course_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        course_not_found.changeVisibility(false)
        error.changeVisibility(false)

        courseInfoRecycler.layoutManager = LinearLayoutManager(context)
        courseInfoRecycler.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(courseInfoRecycler, false)

        setCourseInfo(course)
    }

    fun setCourseInfo(course: Course) {
        organizationTitle.text = SpannableString("by Yandex").apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.course_info_organization_span)), 3, 9, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
        }

        adapter.setData(listOf(
                CourseInfoTextBlock(CourseInfoType.ABOUT, course.description ?: ""),
                CourseInfoTextBlock(CourseInfoType.REQUIREMENTS, course.requirements ?: ""),
                CourseInfoTextBlock(CourseInfoType.TARGET_AUDIENCE, course.targetAudience ?: ""),
                CourseInfoTextBlock(CourseInfoType.TIME_TO_COMPLETE, course.timeToComplete.toString()),
                CourseInfoTextBlock(CourseInfoType.LANGUAGE, course.language ?: ""),
                CourseInfoTextBlock(CourseInfoType.CERTIFICATE, course.certificate ?: ""),

                CourseInfoInstructorsBlock(listOf(User(fullName = "Artyom Burylov", joinDate = null, avatar = "https://stepik.org/media/users/26533986/avatar.png?1523307138", shortBio = """Kotlin backend developer, online education enthusiast. I graduated from PNRPU with a BSc in Computer Science (2014) and MSc in Software Engineering (2016). During the learning, I took an active part in scientific conferences and educational events.""")))
        ))
    }
}