package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_fast_continue.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.presenters.ContinueCoursePresenter
import org.stepic.droid.core.presenters.LastStepPresenter
import org.stepic.droid.core.presenters.PersistentCourseListPresenter
import org.stepic.droid.core.presenters.VideoStepPresenter
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.core.presenters.contracts.LastStepView
import org.stepic.droid.core.presenters.contracts.VideoStepView
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.model.Step
import org.stepic.droid.model.Video
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ThumbnailParser
import timber.log.Timber
import javax.inject.Inject

class FastContinueFragment : FragmentBase(),
        CoursesView,
        ContinueCourseView,
        DroppingListener,
        VideoStepView,
        LastStepView {

    companion object {
        fun newInstance(): FastContinueFragment = FastContinueFragment()
    }

    @Inject
    lateinit var courseListPresenter: PersistentCourseListPresenter

    @Inject
    lateinit var continueCoursePresenter: ContinueCoursePresenter

    @Inject
    lateinit var droppingClient: Client<DroppingListener>

    @Inject
    lateinit var lastStepPresenter: LastStepPresenter

    @Inject
    lateinit var videoStepPresenter: VideoStepPresenter

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
        lastStepPresenter.attachView(this)
        videoStepPresenter.attachView(this)
    }

    override fun onStart() {
        super.onStart()

        //refresh the last course on start
        courseListPresenter.downloadData(Table.enrolled, applyFilter = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoStepPresenter.detachView(this)
        lastStepPresenter.detachView(this)
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
            lastStepPresenter.fetchLastStep(courseId = course.courseId, lastStepId = course.lastStepId)
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


    //LastStepPresenter
    override fun onShowLastStep(step: Step) {
        Timber.d("Step cover is prepared for step.id = ${step.id}")
        if (step.block?.name == AppConstants.TYPE_VIDEO) {
            videoStepPresenter.initVideo(step)
        } else {
            val textForView = step.block?.text
            if (textForView != null && textForView.isNotBlank()) {
                lastStepTextView.text = textResolver.fromHtml(textForView)
            }
        }
    }

    override fun onShowPlaceholder() {
        Timber.d("Show placeholder for step")
    }


    //VideoStepView
    override fun onVideoLoaded(thumbnailPath: String?, cachedVideo: Video?, externalVideo: Video?) {
        if (thumbnailPath == null) {
            return
        }
        val uri = ThumbnailParser.getUriForThumbnail(thumbnailPath)
        Glide
                .with(context)
                .load(uri)
                .into(lastStepImageView)
    }

    override fun onInternetProblem() {
        //no-op
    }

    override fun onNeedOpenVideo(videoId: Long, cachedVideo: Video?, externalVideo: Video?) {
        //no-op
    }

}
