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
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepik.android.view.course_info.model.CourseInfoType
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument
import org.stepik.android.model.Course
import org.stepik.android.model.user.User
import org.stepik.android.presentation.course_info.CourseInfoPresenter
import org.stepik.android.presentation.course_info.CourseInfoView
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

    override fun onStart() {
        super.onStart()
        courseInfoPresenter.attachView(this)
    }

    override fun onStop() {
        courseInfoPresenter.detachView(this)
        super.onStop()
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

//        setCourseInfo(course)
    }

    private fun setCourseInfo(course: Course) {
        val courseInfoItems = mutableListOf(
                CourseInfoItem.OrganizationBlock("Yandex"),
                CourseInfoItem.WithTitle.TextBlock(CourseInfoType.ABOUT, course.description ?: ""),
                CourseInfoItem.WithTitle.TextBlock(CourseInfoType.REQUIREMENTS, course.requirements ?: ""),
                CourseInfoItem.WithTitle.TextBlock(CourseInfoType.TARGET_AUDIENCE, course.targetAudience ?: ""),
                CourseInfoItem.WithTitle.TextBlock(CourseInfoType.TIME_TO_COMPLETE, course.timeToComplete.toString()),
                CourseInfoItem.WithTitle.TextBlock(CourseInfoType.LANGUAGE, course.language ?: ""),
                CourseInfoItem.WithTitle.TextBlock(CourseInfoType.CERTIFICATE, course.certificate ?: ""),

                CourseInfoItem.WithTitle.InstructorsBlock(listOf(User(fullName = "Artyom Burylov", joinDate = null, avatar = "https://stepik.org/media/users/26533986/avatar.png?1523307138", shortBio = """Kotlin backend developer, online education enthusiast. I graduated from PNRPU with a BSc in Computer Science (2014) and MSc in Software Engineering (2016). During the learning, I took an active part in scientific conferences and educational events.""")))
        )

        course.introVideo?.let {
            courseInfoItems.add(CourseInfoItem.VideoBlock(it))
        }

        adapter.setSortedData(courseInfoItems)
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}