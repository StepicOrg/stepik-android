package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_fast_continue.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.presenters.ContinueCoursePresenter
import org.stepic.droid.core.presenters.PersistentCourseListPresenter
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.storage.operations.Table
import javax.inject.Inject

class FastContinueFragment : FragmentBase(),
        CoursesView,
        ContinueCourseView, DroppingListener {

    companion object {
        fun newInstance(): FastContinueFragment = FastContinueFragment()
    }

    @Inject
    lateinit var courseListPresenter: PersistentCourseListPresenter

    @Inject
    lateinit var continueCoursePresenter: ContinueCoursePresenter

    @Inject
    lateinit var droppingClient: Client<DroppingListener>

    override fun injectComponent() {
        App
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onReleaseComponent() {
        App
                .componentManager()
                .releaseCourseGeneralComponent()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_fast_continue, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseListPresenter.attachView(this)
        continueCoursePresenter.attachView(this)
        droppingClient.subscribe(this)
    }

    override fun onStart() {
        super.onStart()

        //refresh the last course on start
        courseListPresenter.downloadData(Table.enrolled, applyFilter = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        courseListPresenter.detachView(this)
        continueCoursePresenter.detachView(this)
        droppingClient.unsubscribe(this)
    }

    //CourseView
    override fun showLoading() {
        // FIXME: 15.09.17 show loading placeholder
    }

    override fun showEmptyCourses() {
        // FIXME: 15.09.17 show suggestion to enroll some course
    }

    override fun showConnectionProblem() {
        // FIXME: 15.09.17 hide the view
    }

    override fun showCourses(courses: MutableList<Course>) {
        // FIXME: 15.09.17 show "Continue" button with the 1st course
        if (courses.isNotEmpty()) {
            // FIXME: 15.09.17 load async the last step of course and in some callback with step show the background
            val course = courses.first()
            fastContinueAction.setOnClickListener {
                continueCoursePresenter.continueCourse(course)
            }
        } else {
            showEmptyCourses()
        }
    }

    //ContinueCourseView
    override fun onShowContinueCourseLoadingDialog() {
        // FIXME: 15.09.17 show loading dialog
    }

    override fun onOpenStep(courseId: Long, section: Section, lessonId: Long, unitId: Long, stepPosition: Int) {
        // FIXME: 15.09.17 dismiss progress dialog
//        ProgressHelper.dismiss(fragmentManager, continueLoadingTag)
        screenManager.continueCourse(activity, courseId, section, lessonId, unitId, stepPosition.toLong())
    }

    override fun onAnyProblemWhileContinue(course: Course) {
        // FIXME: 15.09.17 dismiss progress dialog
        screenManager.showSections(activity, course)
    }

    //Client<DroppingListener>
    override fun onSuccessDropCourse(course: Course) {
        //reload the last course
        courseListPresenter.downloadData(Table.enrolled, applyFilter = false)
    }

    override fun onFailDropCourse(course: Course) {
        //no-op
    }

}
