package org.stepic.droid.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.fragment_fast_continue.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.joining.contract.JoiningListener
import org.stepic.droid.core.presenters.ContinueCoursePresenter
import org.stepic.droid.core.presenters.LastStepPresenter
import org.stepic.droid.core.presenters.PersistentCourseListPresenter
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.*
import javax.inject.Inject

class FastContinueFragment : FragmentBase(),
        CoursesView,
        ContinueCourseView,
        DroppingListener,
        JoiningListener {

    companion object {
        fun newInstance(): FastContinueFragment = FastContinueFragment()

        private const val CONTINUE_LOADING_TAG = "CONTINUE_LOADING_TAG"
    }

    private var isCourseFound: Boolean = false

    @Inject
    lateinit var courseListPresenter: PersistentCourseListPresenter

    @Inject
    lateinit var continueCoursePresenter: ContinueCoursePresenter

    @Inject
    lateinit var droppingClient: Client<DroppingListener>

    @Inject
    lateinit var lastStepPresenter: LastStepPresenter

    @Inject
    lateinit var joiningListenerClient: Client<JoiningListener>

    private lateinit var courseCoverImageViewTarget: BitmapImageViewTarget

    private val coursePlaceholderDrawable by lazy {
        val coursePlaceholderBitmap = BitmapFactory.decodeResource(resources, R.drawable.general_placeholder)
        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, coursePlaceholderBitmap)
        circularBitmapDrawable.cornerRadius = resources.getDimension(R.dimen.course_image_radius)
        return@lazy circularBitmapDrawable
    }

    override fun injectComponent() {
        App
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_fast_continue, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseCoverImageViewTarget = object : BitmapImageViewTarget(fastContinueCourseCover) {
            override fun setResource(resource: Bitmap) {
                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, resource)
                circularBitmapDrawable.cornerRadius = resources.getDimension(R.dimen.course_image_radius)
                fastContinueCourseCover.setImageDrawable(circularBitmapDrawable)
            }
        }

        courseListPresenter.attachView(this)
        continueCoursePresenter.attachView(this)
        droppingClient.subscribe(this)
        joiningListenerClient.subscribe(this)
        courseListPresenter.restoreState()
        fastContinueAction.isEnabled = true


        //refresh the last course only when view is created
        if (sharedPreferenceHelper.authResponseFromStore != null) {
            courseListPresenter.downloadData(Table.enrolled)
        } else {
            analytic.reportEvent(Analytic.FastContinue.AUTH_SHOWN)
            showPlaceholder(R.string.placeholder_login, { _ ->
                analytic.reportEvent(Analytic.FastContinue.AUTH_CLICK)
                screenManager.showLaunchScreen(context, true, MainFeedActivity.HOME_INDEX)
            })
        }

        fastContinueMask.borderRadius = resources.getDimension(R.dimen.course_image_radius)
    }

    override fun onPause() {
        super.onPause()
        ProgressHelper.dismiss(fragmentManager, CONTINUE_LOADING_TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        joiningListenerClient.unsubscribe(this)
        courseListPresenter.detachView(this)
        continueCoursePresenter.detachView(this)
        droppingClient.unsubscribe(this)
    }

    //CourseView
    override fun showLoading() {
        fastContinueProgress.visibility = View.VISIBLE
        showMainGroup(false)
        fastContinuePlaceholder.visibility = View.GONE
    }

    override fun showEmptyCourses() {
        //tbh: courses might be not empty, but not active
        // we can show suggestion for enroll, but not write, that you have zero courses
        analytic.reportEvent(Analytic.FastContinue.EMPTY_COURSES_SHOWN)
        showPlaceholder(R.string.placeholder_explore_courses, { _ ->
            analytic.reportEvent(Analytic.FastContinue.EMPTY_COURSES_CLICK)
            screenManager.showCatalog(context)
        })
    }

    override fun showConnectionProblem() {
        if (isCourseFound) {
            //do not show connection problem, if we have  found the course already
            return
        }

        analytic.reportEvent(Analytic.FastContinue.NO_INTERNET_SHOWN)
        showPlaceholder(R.string.internet_problem, { _ ->
            analytic.reportEvent(Analytic.FastContinue.NO_INTERNET_CLICK)
            if (StepikUtil.isInternetAvailable()) {
                courseListPresenter.downloadData(Table.enrolled)
            }
        })
    }

    override fun showCourses(courses: List<Course>) {
        fastContinueProgress.visibility = View.GONE
        fastContinuePlaceholder.visibility = View.GONE
        showMainGroup(true)
        val course: Course? = courses
                .find {
                    it.isActive
                            && it.sections?.isNotEmpty() ?: false
                }

        if (course != null) {
            if (!isCourseFound) {
                analytic.reportEvent(Analytic.FastContinue.CONTINUE_SHOWN)
            }
            setCourse(course)
            isCourseFound = true
            lastStepPresenter.fetchLastStep(courseId = course.courseId, lastStepId = course.lastStepId)
            fastContinueAction.setOnClickListener {
                analytic.reportEvent(Analytic.FastContinue.CONTINUE_CLICK)
                continueCoursePresenter.continueCourse(course)
            }
        } else {
            isCourseFound = false
            showEmptyCourses()
        }
    }

    private fun setCourse(course: Course) {
        Glide
                .with(context)
                .load(StepikLogicHelper.getPathForCourseOrEmpty(course, config))
                .asBitmap()
                .placeholder(coursePlaceholderDrawable)
                .fitCenter()
                .into(courseCoverImageViewTarget)

        fastContinueCourseName.text = course.title

        val progress = ProgressUtil.getProgressPercent(course.progressObject) ?: 0
        fastContinueCourseProgressText.text = getString(R.string.course_current_progress, progress)

        setCourseProgress(progress)
    }

    private fun setCourseProgress(progress: Int) {
        val parentWidth = (fastContinueCourseProgress.parent as View).measuredWidth
        fastContinueCourseProgress.layoutParams.width = parentWidth * progress / 100
        fastContinueCourseProgress.changeVisibility(progress != 0)
    }

    //ContinueCourseView
    override fun onShowContinueCourseLoadingDialog() {
        // FIXME: 15.09.17  Implement expand/collapse for fastContinueAction

        fastContinueAction.isEnabled = false
        val loadingProgressDialogFragment = LoadingProgressDialogFragment.newInstance()
        if (!loadingProgressDialogFragment.isAdded) {
            loadingProgressDialogFragment.show(fragmentManager, CONTINUE_LOADING_TAG)
        }
    }

    override fun onOpenStep(courseId: Long, section: Section, lessonId: Long, unitId: Long, stepPosition: Int) {
        // FIXME: 15.09.17 expand fastContinueAction

        ProgressHelper.dismiss(fragmentManager, CONTINUE_LOADING_TAG)
        fastContinueAction.isEnabled = true
        screenManager.continueCourse(activity, courseId, section, lessonId, unitId, stepPosition.toLong())
    }

    override fun onAnyProblemWhileContinue(course: Course) {
        // FIXME: 15.09.17 expand fastContinueAction

        ProgressHelper.dismiss(fragmentManager, CONTINUE_LOADING_TAG)
        fastContinueAction.isEnabled = true
        screenManager.showSections(activity, course)
    }

    //Client<DroppingListener>
    override fun onSuccessDropCourse(course: Course) {
        //reload the last course
        courseListPresenter.refreshData(Table.enrolled)
    }

    override fun onFailDropCourse(course: Course) {
        //no-op
    }

    private fun showPlaceholder(@StringRes stringRes: Int, listener: (view: View) -> Unit) {
        fastContinueProgress.visibility = View.GONE
        fastContinuePlaceholder.setPlaceholderText(stringRes)
        fastContinuePlaceholder.setOnClickListener(listener)
        showMainGroup(false)
        fastContinuePlaceholder.visibility = View.VISIBLE
    }

    private fun showMainGroup(needShow: Boolean) {
        val visibility = if (needShow) {
            View.VISIBLE
        } else {
            View.GONE
        }
        fastContinueAction.visibility = visibility
        fastContinueOverlay.visibility = visibility
        fastContinueMask.visibility = visibility
    }

    override fun onSuccessJoin(joinedCourse: Course) {
        showCourses(mutableListOf(joinedCourse))
    }
}
