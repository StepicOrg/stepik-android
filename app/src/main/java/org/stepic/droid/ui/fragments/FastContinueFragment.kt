package org.stepic.droid.ui.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.fragment_fast_continue.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.joining.contract.JoiningListener
import org.stepic.droid.core.presenters.ContinueCoursePresenter
import org.stepic.droid.core.presenters.FastContinuePresenter
import org.stepic.droid.core.presenters.PersistentCourseListPresenter
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.core.presenters.contracts.FastContinueView
import org.stepic.droid.model.CourseListType
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import javax.inject.Inject

class FastContinueFragment : FragmentBase(),
        ContinueCourseView,
        DroppingListener,
        JoiningListener,
        FastContinueView {

    companion object {
        fun newInstance(): FastContinueFragment = FastContinueFragment()

        private const val CONTINUE_LOADING_TAG = "CONTINUE_LOADING_TAG"
    }

    @Inject
    lateinit var courseListPresenter: PersistentCourseListPresenter

    @Inject
    lateinit var continueCoursePresenter: ContinueCoursePresenter

    @Inject
    lateinit var droppingClient: Client<DroppingListener>

    @Inject
    lateinit var joiningListenerClient: Client<JoiningListener>

    @Inject
    lateinit var fastContinuePresenter: FastContinuePresenter

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

    override fun onShowCourse(course: Course) {
        fastContinueProgress.visibility = View.GONE
        fastContinuePlaceholder.visibility = View.GONE

        analytic.reportEvent(Analytic.FastContinue.CONTINUE_SHOWN)
        setCourse(course)
        showMainGroup(true)
        fastContinueOverlay.setOnClickListener { handleContinueCourseClick(course) }
        fastContinueAction.setOnClickListener { handleContinueCourseClick(course) }
    }

    override fun onLoading() {
        fastContinueProgress.visibility = View.VISIBLE
        showMainGroup(false)
        fastContinuePlaceholder.visibility = View.GONE
    }


    private fun setCourse(course: Course) {
        Glide
            .with(requireContext())
            .asBitmap()
            .load(course.cover)
            .placeholder(coursePlaceholderDrawable)
            .fitCenter()
            .into(courseCoverImageViewTarget)

        fastContinueCourseName.text = course.title

//        val progress = course.progressObject
//        val needShow = if (progress != null && progress.cost > 0) {
//            val score = progress
//                .score
//                ?.toFloatOrNull()
//                ?.toLong()
//                ?: 0L
//
//            fastContinueCourseProgressText.text = getString(R.string.course_current_progress, score, progress.cost)
//            fastContinueCourseProgress.progress = (score * 100 / progress.cost).toInt()
//            true
//        } else {
//            fastContinueCourseProgress.progress = 0
//            false
//        }
//        fastContinueCourseProgressText.isVisible = needShow
    }

    //ContinueCourseView
    override fun onShowContinueCourseLoadingDialog() {
        fastContinueOverlay.isEnabled = false
        fastContinueAction.isEnabled = false
        val loadingProgressDialogFragment = LoadingProgressDialogFragment.newInstance()
        if (!loadingProgressDialogFragment.isAdded) {
            loadingProgressDialogFragment.show(requireFragmentManager(), CONTINUE_LOADING_TAG)
        }
    }

    override fun onOpenStep(courseId: Long, lastStep: LastStep) {
        ProgressHelper.dismiss(fragmentManager, CONTINUE_LOADING_TAG)
        fastContinueOverlay.isEnabled = true
        fastContinueAction.isEnabled = true
        screenManager.continueCourse(activity, courseId, lastStep)
    }

    override fun onOpenAdaptiveCourse(course: Course) {
        ProgressHelper.dismiss(fragmentManager, CONTINUE_LOADING_TAG)
        fastContinueOverlay.isEnabled = true
        fastContinueAction.isEnabled = true
        screenManager.continueAdaptiveCourse(activity, course)
    }

    override fun onAnyProblemWhileContinue(course: Course) {
        ProgressHelper.dismiss(fragmentManager, CONTINUE_LOADING_TAG)
        fastContinueOverlay.isEnabled = true
        fastContinueAction.isEnabled = true
        screenManager.showCourseModules(activity, course)
    }

    //Client<DroppingListener>
    override fun onSuccessDropCourse(course: Course) {
        //reload the last course
        courseListPresenter.refreshData(CourseListType.ENROLLED)
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
        onShowCourse(joinedCourse)
    }

    private fun handleContinueCourseClick(course: Course) {
        analytic.reportEvent(Analytic.FastContinue.CONTINUE_CLICK)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
            AmplitudeAnalytic.Course.Params.COURSE to course.id,
            AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.HOME_WIDGET
        ))
        continueCoursePresenter.continueCourse(course)
    }
}
