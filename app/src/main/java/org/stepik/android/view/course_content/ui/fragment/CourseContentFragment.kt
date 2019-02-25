package org.stepik.android.view.course_content.ui.fragment

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables.zip
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.empty_default.*
import kotlinx.android.synthetic.main.error_no_connection.*
import kotlinx.android.synthetic.main.fragment_course_content.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.VideoQualityDetailedDialog
import org.stepic.droid.ui.util.PopupHelper
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_deadlines.model.Deadline
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.presentation.course_content.CourseContentPresenter
import org.stepik.android.presentation.course_content.CourseContentView
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.ui.adapter.CourseContentAdapter
import org.stepik.android.view.course_content.ui.adapter.delegates.control_bar.CourseContentControlBarClickListener
import org.stepik.android.view.course_content.ui.fragment.listener.CourseContentSectionClickListenerImpl
import org.stepik.android.view.course_content.ui.fragment.listener.CourseContentUnitClickListenerImpl
import org.stepik.android.view.personal_deadlines.ui.dialogs.EditDeadlinesDialog
import org.stepik.android.view.personal_deadlines.ui.dialogs.LearningRateDialog
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import org.stepik.android.view.ui.listener.FragmentViewPagerScrollStateListener
import javax.inject.Inject

class CourseContentFragment : Fragment(), CourseContentView, FragmentViewPagerScrollStateListener {
    companion object {
        fun newInstance(courseId: Long): Fragment  =
            CourseContentFragment().apply {
                this.courseId = courseId
            }

        private val SCROLL_STATE_IDLE_STUB = RecyclerView.SCROLL_STATE_IDLE to 0
        private val SCROLL_STATE_SCROLLING_STUB = RecyclerView.SCROLL_STATE_DRAGGING to -1
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var analytic: Analytic

    private lateinit var contentAdapter: CourseContentAdapter
    private var courseId: Long by argument()

    private lateinit var courseContentPresenter: CourseContentPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<CourseContentView.State>

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private val fragmentVisibilitySubject =
        BehaviorSubject.create<FragmentViewPagerScrollStateListener.ScrollState>()

    private val contentRecyclerScrollStateSubject =
        BehaviorSubject.createDefault(SCROLL_STATE_IDLE_STUB)

    private val uiCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent(courseId)

        courseContentPresenter = ViewModelProviders.of(this, viewModelFactory).get(CourseContentPresenter::class.java)
        savedInstanceState?.let(courseContentPresenter::onRestoreInstanceState)
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
        inflater.inflate(R.layout.fragment_course_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(courseContentRecycler) {
            contentAdapter =
                CourseContentAdapter(
                    sectionClickListener    = CourseContentSectionClickListenerImpl(context, courseContentPresenter, screenManager),
                    unitClickListener       = CourseContentUnitClickListenerImpl(activity, courseContentPresenter, screenManager),
                    controlBarClickListener = object : CourseContentControlBarClickListener {
                        override fun onCreateScheduleClicked() {
                            showPersonalDeadlinesLearningRateDialog()
                        }

                        override fun onChangeScheduleClicked(record: StorageRecord<DeadlinesWrapper>) {
                            showPersonalDeadlinesEditDialog(record)
                        }

                        override fun onRemoveScheduleClicked(record: StorageRecord<DeadlinesWrapper>) {
                            courseContentPresenter.removeDeadlines()
                        }

                        override fun onDownloadAllClicked(course: Course) {
                            courseContentPresenter.addCourseDownloadTask(course)
                        }
                    }
                )

            val linearLayoutManager = LinearLayoutManager(context)
            adapter = contentAdapter
            layoutManager = linearLayoutManager
            itemAnimator = null

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.list_divider_h)?.let(::setDrawable)
            })

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        contentRecyclerScrollStateSubject.onNext(newState to linearLayoutManager.findFirstCompletelyVisibleItemPosition())
                    } else {
                        contentRecyclerScrollStateSubject.onNext(SCROLL_STATE_SCROLLING_STUB)
                    }
                }
            })
        }

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<CourseContentView.State.Idle>(courseContentPlaceholder)
        viewStateDelegate.addState<CourseContentView.State.Loading>(courseContentPlaceholder)
        viewStateDelegate.addState<CourseContentView.State.CourseContentLoaded>(courseContentRecycler)
        viewStateDelegate.addState<CourseContentView.State.NetworkError>(reportProblem)
        viewStateDelegate.addState<CourseContentView.State.EmptyContent>(report_empty)
    }

    override fun onStart() {
        super.onStart()
        courseContentPresenter.attachView(this)
    }

    override fun onStop() {
        courseContentPresenter.detachView(this)
        super.onStop()
    }

    override fun onViewPagerScrollStateChanged(scrollState: FragmentViewPagerScrollStateListener.ScrollState) {
        fragmentVisibilitySubject.onNext(scrollState)
    }

    /**
     * States
     */
    override fun setState(state: CourseContentView.State) {
        viewStateDelegate.switchState(state)
        if (state is CourseContentView.State.CourseContentLoaded) {
            contentAdapter.items = state.courseContent
            contentAdapter.setControlBar(CourseContentItem.ControlBar(state.course.enrollment > 0, state.personalDeadlinesState, state.course))
        }
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, activity?.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(activity?.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    /**
     * Downloads
     */
    override fun updateSectionDownloadProgress(downloadProgress: DownloadProgress) {
        contentAdapter.updateSectionDownloadProgress(downloadProgress)
    }

    override fun updateUnitDownloadProgress(downloadProgress: DownloadProgress) {
        contentAdapter.updateUnitDownloadProgress(downloadProgress)
    }

    override fun showChangeDownloadNetworkType() {
        val view = view
            ?: return

        Snackbar
            .make(view, R.string.allow_mobile_snack, Snackbar.LENGTH_LONG)
            .setAction(R.string.settings_title) {
                analytic.reportEvent(Analytic.Downloading.CLICK_SETTINGS_SECTIONS)
                screenManager.showSettings(activity)
            }
            .setActionTextColor(ContextCompat.getColor(view.context, R.color.snack_action_color))
            .setTextColor(ContextCompat.getColor(view.context, R.color.white))
            .show()
    }

    override fun showVideoQualityDialog(course: Course?, section: Section?, unit: Unit?) {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?.takeIf { it.findFragmentByTag(VideoQualityDetailedDialog.TAG) == null }
            ?: return

        val dialog = VideoQualityDetailedDialog.newInstance(course, section, unit)
        dialog.setTargetFragment(this, VideoQualityDetailedDialog.VIDEO_QUALITY_REQUEST_CODE)
        dialog.show(supportFragmentManager, VideoQualityDetailedDialog.TAG)
    }

    /**
     * Personal deadlines
     */
    override fun showPersonalDeadlinesBanner() {
        val visibilityObservable = fragmentVisibilitySubject
            .filter { it == FragmentViewPagerScrollStateListener.ScrollState.ACTIVE }

        val scrollObservable = contentRecyclerScrollStateSubject
            .filter { (state, firstVisiblePosition) -> state == RecyclerView.SCROLL_STATE_IDLE && firstVisiblePosition == 0 }

        uiCompositeDisposable += zip(visibilityObservable, scrollObservable)
            .firstElement()
            .ignoreElement()
            .subscribe {
                val anchorView = courseContentRecycler.findViewById<View>(R.id.course_control_schedule)
                val deadlinesDescription = getString(R.string.deadlines_banner_description)
                PopupHelper.showPopupAnchoredToView(requireContext(), anchorView, deadlinesDescription, cancelableOnTouchOutside = true)
            }
    }

    override fun showPersonalDeadlinesBannerAB() {
        val visibilityObservable = fragmentVisibilitySubject
            .filter { it == FragmentViewPagerScrollStateListener.ScrollState.ACTIVE }

        val scrollObservable = contentRecyclerScrollStateSubject
            .filter { (state, firstVisiblePosition) -> state == RecyclerView.SCROLL_STATE_IDLE && firstVisiblePosition == 0 }

        uiCompositeDisposable += zip(visibilityObservable, scrollObservable)
            .firstElement()
            .ignoreElement()
            .subscribe {
                val anchorView = courseContentRecycler.findViewById<View>(R.id.course_control_schedule)
                val deadlinesDescription = getString(R.string.deadlines_ab_banner_description)
                PopupHelper.showPopupAnchoredToView(requireContext(), anchorView, deadlinesDescription, cancelableOnTouchOutside = true)
            }
    }

    override fun showPersonalDeadlinesError() {
        val view = view
            ?: return

        Snackbar
            .make(view, R.string.deadlines_fetching_error, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(view.context, R.color.white))
            .show()
    }

    private fun showPersonalDeadlinesLearningRateDialog() {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?.takeIf { it.findFragmentByTag(LearningRateDialog.TAG) == null }
            ?: return

        val dialog = LearningRateDialog.newInstance()
        dialog.setTargetFragment(this, LearningRateDialog.LEARNING_RATE_REQUEST_CODE)
        dialog.show(supportFragmentManager, LearningRateDialog.TAG)
    }

    private fun showPersonalDeadlinesEditDialog(record: StorageRecord<DeadlinesWrapper>) {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?.takeIf { it.findFragmentByTag(EditDeadlinesDialog.TAG) == null }
            ?: return

        val sections = contentAdapter
            .items
            .mapNotNull { item ->
                (item as? CourseContentItem.SectionItem)
                    ?.section
            }

        val dialog = EditDeadlinesDialog.newInstance(sections, record)
        dialog.setTargetFragment(this, EditDeadlinesDialog.EDIT_DEADLINES_REQUEST_CODE)
        dialog.show(supportFragmentManager, EditDeadlinesDialog.TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LearningRateDialog.LEARNING_RATE_REQUEST_CODE ->
                data?.takeIf { resultCode == Activity.RESULT_OK }
                    ?.getParcelableExtra<LearningRate>(LearningRateDialog.KEY_LEARNING_RATE)
                    ?.let(courseContentPresenter::createPersonalDeadlines)

            EditDeadlinesDialog.EDIT_DEADLINES_REQUEST_CODE ->
                data?.takeIf { resultCode == Activity.RESULT_OK }
                    ?.getParcelableArrayListExtra<Deadline>(EditDeadlinesDialog.KEY_DEADLINES)
                    ?.let(courseContentPresenter::updatePersonalDeadlines)

            VideoQualityDetailedDialog.VIDEO_QUALITY_REQUEST_CODE ->
                data?.let { intent ->
                    val videoQuality = intent
                        .getStringExtra(VideoQualityDetailedDialog.VIDEO_QUALITY)
                        ?: return

                    val course: Course? = intent
                        .getParcelableExtra(VideoQualityDetailedDialog.COURSE_KEY)
                    if (course != null) {
                        return courseContentPresenter.addCourseDownloadTask(course, videoQuality)
                    }

                    val section: Section? = intent
                        .getParcelableExtra(VideoQualityDetailedDialog.SECTION_KEY)
                    if (section != null) {
                        return courseContentPresenter.addSectionDownloadTask(section, videoQuality)
                    }

                    val unit: Unit? = intent
                        .getParcelableExtra(VideoQualityDetailedDialog.UNIT_KEY)
                    if (unit != null) {
                        return courseContentPresenter.addUnitDownloadTask(unit, videoQuality)
                    }
                }

            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        uiCompositeDisposable.clear()
        super.onDestroyView()
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}
