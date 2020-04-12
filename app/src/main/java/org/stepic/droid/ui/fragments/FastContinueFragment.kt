package org.stepic.droid.ui.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
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
import org.stepic.droid.core.presenters.FastContinuePresenter
import org.stepic.droid.core.presenters.contracts.FastContinueView
import org.stepic.droid.model.CourseListType
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import javax.inject.Inject

class FastContinueFragment : FragmentBase(),
        CourseContinueView,
        DroppingListener,
        JoiningListener,
        FastContinueView {

    companion object {
        fun newInstance(): FastContinueFragment = FastContinueFragment()

        private const val CONTINUE_LOADING_TAG = "CONTINUE_LOADING_TAG"
    }

    @Inject
    lateinit var continueCoursePresenter: ContinueCoursePresenter

    @Inject
    lateinit var droppingClient: Client<DroppingListener>

    @Inject
    lateinit var joiningListenerClient: Client<JoiningListener>

    @Inject
    lateinit var fastContinuePresenter: FastContinuePresenter

    private lateinit var courseCoverImageViewTarget: BitmapImageViewTarget

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_fast_continue, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseCoverImageViewTarget = RoundedBitmapImageViewTarget(resources.getDimension(R.dimen.course_image_radius), fastContinueCourseCover)

        continueCoursePresenter.attachView(this)
        droppingClient.subscribe(this)
        joiningListenerClient.subscribe(this)
        fastContinueOverlay.isEnabled = true
        fastContinueAction.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        fastContinuePresenter.attachView(this)
        fastContinuePresenter.onCreated()
    }

    override fun onPause() {
        fastContinuePresenter.detachView(this)
        super.onPause()
        ProgressHelper.dismiss(fragmentManager, CONTINUE_LOADING_TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        joiningListenerClient.unsubscribe(this)
        continueCoursePresenter.detachView(this)
        droppingClient.unsubscribe(this)
    }

    override fun onAnonymous() {
        analytic.reportEvent(Analytic.FastContinue.AUTH_SHOWN)
        showPlaceholder(R.string.placeholder_login) {
            analytic.reportEvent(Analytic.FastContinue.AUTH_CLICK)
            screenManager.showLaunchScreen(context, true, MainFeedActivity.HOME_INDEX)
        }
    }

    override fun onEmptyCourse() {
        // tbh: courses might be not empty, but not active
        // we can show suggestion for enroll, but not write, that you have zero courses
        analytic.reportEvent(Analytic.FastContinue.EMPTY_COURSES_SHOWN)
        showPlaceholder(R.string.placeholder_explore_courses) {
            analytic.reportEvent(Analytic.FastContinue.EMPTY_COURSES_CLICK)
            screenManager.showCatalog(context)
        }
    }

    override fun onShowCourse(courseListItem: CourseListItem.Data) {
        fastContinueProgress.visibility = View.GONE
        fastContinuePlaceholder.visibility = View.GONE

        analytic.reportEvent(Analytic.FastContinue.CONTINUE_SHOWN)
        setCourse(courseListItem)
        showMainGroup(true)
        fastContinueOverlay.setOnClickListener { handleContinueCourseClick(courseListItem.course) }
        fastContinueAction.setOnClickListener { handleContinueCourseClick(courseListItem.course) }
    }

    override fun onLoading() {
        fastContinueProgress.visibility = View.VISIBLE
        showMainGroup(false)
        fastContinuePlaceholder.visibility = View.GONE
    }


    private fun setCourse(courseListItem: CourseListItem.Data) {
        Glide
            .with(requireContext())
            .asBitmap()
            .load(courseListItem.course.cover)
            .placeholder(coursePlaceholderDrawable)
            .fitCenter()
            .into(courseCoverImageViewTarget)

        fastContinueCourseName.text = courseListItem.course.title

        val progress = courseListItem.courseStats.progress
        val needShow = if (progress != null && progress.cost > 0) {
            val score = progress
                .score
                ?.toFloatOrNull()
                ?.toLong()
                ?: 0L

            fastContinueCourseProgressText.text = getString(R.string.course_current_progress, score, progress.cost)
            fastContinueCourseProgress.progress = (score * 100 / progress.cost).toInt()
            true
        } else {
            fastContinueCourseProgress.progress = 0
            false
        }
        fastContinueCourseProgressText.isVisible = needShow
    }

    //Client<DroppingListener>
    override fun onSuccessDropCourse(course: Course) {
        //reload the last course
        // TODO Check DroppingListener
//        courseListPresenter.refreshData(CourseListType.ENROLLED)
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
        fastContinueMask.isVisible = needShow
    }

    override fun onSuccessJoin(joinedCourse: Course) {
        // onShowCourse(joinedCourse)
    }

    private fun handleContinueCourseClick(course: Course) {
        analytic.reportEvent(Analytic.FastContinue.CONTINUE_CLICK)
        continueCoursePresenter.continueCourse(course, CourseContinueInteractionSource.HOME_WIDGET)
    }

    override fun showCourse(course: Course, isAdaptive: Boolean) {
        if (isAdaptive) {
            screenManager.continueAdaptiveCourse(activity, course)
        } else {
            screenManager.showCourseModules(activity, course)
        }
    }

    override fun showSteps(course: Course, lastStep: LastStep) {
        screenManager.continueCourse(activity, course.id, lastStep)
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        fastContinueOverlay.isEnabled = !isLoading
        fastContinueAction.isEnabled = !isLoading
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, fragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(fragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }
}
