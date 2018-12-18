package org.stepik.android.view.course_content.ui.fragment

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
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
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.features.deadlines.model.Deadline
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.view.course_content.ui.adapter.CourseContentAdapter
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitClickListener
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.util.argument
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import org.stepik.android.presentation.course_content.CourseContentPresenter
import org.stepik.android.presentation.course_content.CourseContentView
import org.stepik.android.view.course_content.ui.adapter.delegates.control_bar.CourseContentControlBarClickListener
import org.stepik.android.view.course_content.ui.adapter.delegates.section.CourseContentSectionClickListener
import org.stepik.android.view.personal_deadlines.ui.dialogs.EditDeadlinesDialog
import org.stepik.android.view.personal_deadlines.ui.dialogs.LearningRateDialog
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseContentFragment : Fragment(), CourseContentView {
    companion object {
        fun newInstance(courseId: Long) =
            CourseContentFragment().apply {
                this.courseId = courseId
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    private lateinit var contentAdapter: CourseContentAdapter
    private var courseId: Long by argument()

    private lateinit var courseContentPresenter: CourseContentPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<CourseContentView.State>

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
                    sectionClickListener = object : CourseContentSectionClickListener {
                        override fun onItemDownloadClicked(item: CourseContentItem.SectionItem) {
                            courseContentPresenter.addSectionDownloadTask(item.section)
                        }

                        override fun onItemRemoveClicked(item: CourseContentItem.SectionItem) {
                            courseContentPresenter.removeSectionDownloadTask(item.section)
                        }
                    },
                    unitClickListener = object : CourseContentUnitClickListener {
                        override fun onItemClicked(item: CourseContentItem.UnitItem) {
                            screenManager.showSteps(activity, item.unit, item.lesson, item.section)
                        }

                        override fun onItemDownloadClicked(item: CourseContentItem.UnitItem) {
                            courseContentPresenter.addUnitDownloadTask(item.unit)
                        }

                        override fun onItemRemoveClicked(item: CourseContentItem.UnitItem) {
                            courseContentPresenter.removeUnitDownloadTask(item.unit)
                        }
                    },
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
                    }
                )

            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.list_divider_h)?.let(::setDrawable)
            })
        }

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<CourseContentView.State.Idle>(courseContentPlaceholder)
        viewStateDelegate.addState<CourseContentView.State.Loading>(courseContentPlaceholder)
        viewStateDelegate.addState<CourseContentView.State.CourseContentLoaded>(courseContentRecycler)
        viewStateDelegate.addState<CourseContentView.State.NetworkError>(error)
    }

    override fun onStart() {
        super.onStart()
        courseContentPresenter.attachView(this)
    }

    override fun onStop() {
        courseContentPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: CourseContentView.State) {
        viewStateDelegate.switchState(state)
        if (state is CourseContentView.State.CourseContentLoaded) {
            contentAdapter.items = state.courseContent
            contentAdapter.setControlBar(CourseContentItem.ControlBar(state.course.enrollment > 0, state.personalDeadlinesState))
        }
    }

    override fun updateSectionDownloadProgress(downloadProgress: DownloadProgress) {
        contentAdapter.updateSectionDownloadProgress(downloadProgress)
    }

    override fun updateUnitDownloadProgress(downloadProgress: DownloadProgress) {
        contentAdapter.updateUnitDownloadProgress(downloadProgress)
    }

    override fun showPersonalDeadlinesBanner() {

    }

    override fun showPersonalDeadlinesError() {

    }

    /**
     * Personal deadlines
     */
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
        when(requestCode) {
            LearningRateDialog.LEARNING_RATE_REQUEST_CODE ->
                data?.takeIf { resultCode == Activity.RESULT_OK }
                    ?.getParcelableExtra<LearningRate>(LearningRateDialog.KEY_LEARNING_RATE)
                    ?.let(courseContentPresenter::createPersonalDeadlines)

            EditDeadlinesDialog.EDIT_DEADLINES_REQUEST_CODE ->
                data?.takeIf { resultCode == Activity.RESULT_OK }
                    ?.getParcelableArrayListExtra<Deadline>(EditDeadlinesDialog.KEY_DEADLINES)
                    ?.let(courseContentPresenter::updatePersonalDeadlines)

            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}
