package org.stepik.android.view.solutions.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_solutions.*
import kotlinx.android.synthetic.main.empty_default.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.mutate
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Submission
import org.stepik.android.presentation.solutions.SolutionsPresenter
import org.stepik.android.presentation.solutions.SolutionsView
import org.stepik.android.view.solutions.ui.adapter.delegate.SolutionDisclaimerDelegate
import org.stepik.android.view.solutions.ui.adapter.delegate.SolutionLessonAdapterDelegate
import org.stepik.android.view.solutions.ui.adapter.delegate.SolutionSectionAdapterDelegate
import org.stepik.android.view.solutions.ui.adapter.delegate.SolutionSubmissionAdapterDelegate
import org.stepik.android.view.solutions.ui.dialog.RemoveSolutionsDialog
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.ui.adapters.selection.MultipleChoiceSelectionHelper
import ru.nobird.android.view.base.ui.extension.getDrawableCompat
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.base.ui.extension.snackbar
import java.util.ArrayList
import javax.inject.Inject

class SolutionsActivity : FragmentActivityBase(), SolutionsView, RemoveSolutionsDialog.Callback {
    companion object {
        private const val EVALUATION_FRAME_DURATION_MS = 250
        private const val CHECKED_ITEMS_ARGUMENT = "checked_items"

        private const val EXTRA_COURSE_ID = "course_id"

        fun createIntent(context: Context, courseId: Long): Intent =
            Intent(context, SolutionsActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var solutionsPresenter: SolutionsPresenter

    private var isDeleteMenuItemVisible = false

    private var solutionsAdapter: DefaultDelegateAdapter<SolutionItem> = DefaultDelegateAdapter()
    private val selectionHelper = MultipleChoiceSelectionHelper(solutionsAdapter)
    private val checkedIndices = ArrayList<Int>()

    private val viewStateDelegate =
        ViewStateDelegate<SolutionsView.State>()

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solutions)

        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1)
        injectComponent(courseId)

        solutionsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(SolutionsPresenter::class.java)
        initCenteredToolbar(R.string.solutions_toolbar_title, showHomeButton = true)

        solutionsAdapter += SolutionDisclaimerDelegate()
        solutionsAdapter += SolutionSectionAdapterDelegate(selectionHelper, onClick = ::handleSectionCheckboxClick)
        solutionsAdapter += SolutionLessonAdapterDelegate(selectionHelper, onClick = ::handleLessonCheckboxClick)
        solutionsAdapter += SolutionSubmissionAdapterDelegate(
            selectionHelper,
            onCheckboxClick = ::handleSubmissionCheckBoxClick,
            onItemClick = ::handleSubmissionItemClick
        )

        with(solutionsRecycler) {
            itemAnimator = null
            adapter = solutionsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(DividerItemDecoration(this@SolutionsActivity, LinearLayoutManager.VERTICAL).apply {
                ContextCompat.getDrawable(this@SolutionsActivity, R.drawable.list_divider_h)?.let(::setDrawable)
            })
        }

        val evaluationDrawable = AnimationDrawable()
        evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.isOneShot = false

        solutionsSubmitFeedback.setCompoundDrawablesWithIntrinsicBounds(evaluationDrawable, null, null, null)
        evaluationDrawable.start()

        solutionsSubmitButton.setOnClickListener {
            if (fetchSelectedSubmissionItems().isEmpty()) {
                for (index in solutionsAdapter.items.indices) {
                    selectionHelper.select(index)
                }
            }
            val selectedSubmissionItems = fetchSelectedSubmissionItems()
            solutionsPresenter.submitSolutions(selectedSubmissionItems)
        }

        initViewStateDelegate()
        solutionsPresenter.fetchSolutionItems(localOnly = true)
        tryAgain.setOnClickListener { solutionsPresenter.fetchSolutionItems(localOnly = false) }

        if (savedInstanceState != null && savedInstanceState.containsKey(CHECKED_ITEMS_ARGUMENT)) {
            checkedIndices.addAll(savedInstanceState.getIntegerArrayList(CHECKED_ITEMS_ARGUMENT) as ArrayList<Int>)
        }
    }

    private fun injectComponent(courseId: Long) {
        App.component()
            .solutionsComponentBuilder()
            .courseId(courseId)
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        solutionsPresenter.attachView(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val items = solutionsAdapter.items.withIndex().filter { selectionHelper.isSelected(it.index) }.map { it.index }
        outState.putIntegerArrayList(CHECKED_ITEMS_ARGUMENT, items as ArrayList<Int>)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        solutionsPresenter.detachView(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.attempts_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val selectedCount = fetchSelectedSubmissionItems().size
        menu.findItem(R.id.attempts_menu_item_delete).isVisible = selectedCount != 0 && isDeleteMenuItemVisible
        if (selectedCount == 0) {
            centeredToolbarTitle.text = getString(R.string.solutions_toolbar_title)
            solutionsSubmitButton.text = getString(R.string.solutions_submit_all)
        } else {
            centeredToolbarTitle.text = getString(R.string.solutions_selected, selectedCount)
            solutionsSubmitButton.text = resources.getQuantityString(R.plurals.submit_solutions, selectedCount, selectedCount)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.attempts_menu_item_delete -> {
                val items = fetchSelectedSubmissionItems()
                val attemptIds = items.map { it.submission.attempt }
                showRemoveAttemptsDialog(attemptIds)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<SolutionsView.State.Idle>()
        viewStateDelegate.addState<SolutionsView.State.Loading>(loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<SolutionsView.State.Empty>(report_empty)
        viewStateDelegate.addState<SolutionsView.State.Error>(error)
        viewStateDelegate.addState<SolutionsView.State.SolutionsLoaded>(
            solutionsRecycler,
            solutionsSubmissionSeparator,
            solutionsSubmitButton
        )
    }

    override fun setState(state: SolutionsView.State) {
        viewStateDelegate.switchState(state)
        isDeleteMenuItemVisible =
            (state as? SolutionsView.State.SolutionsLoaded)?.isSending == false

        if (state is SolutionsView.State.SolutionsLoaded) {
            solutionsAdapter.items = state.solutions.mutate { add(0, SolutionItem.Disclaimer) }
            solutionsSubmitButton.isEnabled = !state.isSending
            solutionsSubmitFeedback.isVisible = state.isSending
            if (checkedIndices.isNotEmpty()) {
                checkedIndices.forEach { selectionHelper.select(it) }
            }
        }

        invalidateOptionsMenu()
    }

    override fun showNetworkError() {
        root.snackbar(messageRes = R.string.no_connection)
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun onAttemptRemoveConfirmed(attemptIds: List<Long>) {
        solutionsPresenter.removeSolutions(attemptIds)
        checkedIndices.clear()
        selectionHelper.reset()
        invalidateOptionsMenu()
    }

    override fun onFinishedSending() {
        selectionHelper.reset()
        invalidateOptionsMenu()
    }

    private fun showRemoveAttemptsDialog(attemptIds: List<Long>) {
        RemoveSolutionsDialog
            .newInstance(attemptIds = attemptIds)
            .showIfNotExists(supportFragmentManager, RemoveSolutionsDialog.TAG)
    }

    /**
     *  isSelected functions
     */

    private fun isAllSubmissionsSelectedInLesson(lessonIndex: Int): Boolean {
        var areAllSelectedInLesson = true
        for (index in lessonIndex + 1 until solutionsAdapter.items.size) {
            val item = solutionsAdapter.items[index]
            if (item is SolutionItem.SubmissionItem) {
                if (item.submission.status == Submission.Status.LOCAL) {
                    areAllSelectedInLesson =
                        areAllSelectedInLesson && selectionHelper.isSelected(index)
                } else {
                    continue
                }
            } else {
                break
            }
        }
        return areAllSelectedInLesson
    }

    private fun isAllLessonsSelectedInSection(sectionIndex: Int, sectionId: Long): Boolean {
        var areAllSelectedInSection = true
        for (index in sectionIndex + 1 until solutionsAdapter.items.size) {
            val item = solutionsAdapter.items[index]
            if (item is SolutionItem.LessonItem) {
                if (item.section.id == sectionId) {
                    areAllSelectedInSection = areAllSelectedInSection && selectionHelper.isSelected(index)
                } else {
                    break
                }
            }
        }
        return areAllSelectedInSection
    }

    /**
     *  Check box click functions
     */

    private fun handleSectionCheckboxClick(solutionSectionItem: SolutionItem.SectionItem) {
        val itemIndex = solutionsAdapter.items.indexOf(solutionSectionItem)
        val sectionId = solutionSectionItem.section.id
        selectionHelper.toggle(itemIndex)
        val isSelected = selectionHelper.isSelected(itemIndex)

        for (index in itemIndex + 1 until solutionsAdapter.items.size) {
            val itemSectionId = when (val item = solutionsAdapter.items[index]) {
                is SolutionItem.LessonItem ->
                    item.section.id
                is SolutionItem.SubmissionItem ->
                    item.section.id
                else ->
                    -1L
            }

            if (itemSectionId == sectionId) {
                if (isSelected) {
                    selectionHelper.select(index)
                } else {
                    selectionHelper.deselect(index)
                }
            } else {
                break
            }
        }
        invalidateOptionsMenu()
    }

    private fun handleLessonCheckboxClick(solutionLessonItem: SolutionItem.LessonItem) {
        val itemIndex = solutionsAdapter.items.indexOf(solutionLessonItem)
        val lessonId = solutionLessonItem.lesson.id
        val sectionId = solutionLessonItem.section.id
        selectionHelper.toggle(itemIndex)
        val isSelected = selectionHelper.isSelected(itemIndex)

        for (index in itemIndex + 1 until solutionsAdapter.items.size) {
            val item = solutionsAdapter.items[index]
            if (item is SolutionItem.SubmissionItem && item.lesson.id == lessonId) {
                    if (isSelected) {
                        selectionHelper.select(index)
                    } else {
                        selectionHelper.deselect(index)
                    }
                } else {
                break
            }
        }

        val sectionIndex = solutionsAdapter.items.indexOfFirst { item ->
            item is SolutionItem.SectionItem && item.section.id == sectionId
        }

        if (isAllLessonsSelectedInSection(sectionIndex, sectionId)) {
            selectionHelper.select(sectionIndex)
        } else {
            selectionHelper.deselect(sectionIndex)
        }
        invalidateOptionsMenu()
    }

    private fun handleSubmissionItemClick(solutionSubmissionItem: SolutionItem.SubmissionItem) {
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.LocalSubmissions.LOCAL_SUBMISSION_ITEM_CLICKED, mapOf(
                AmplitudeAnalytic.LocalSubmissions.Params.STEP_ID to solutionSubmissionItem.step.id
            )
        )
        val step = LastStep(
            id = "",
            unit = solutionSubmissionItem.unit.id,
            lesson = solutionSubmissionItem.lesson.id,
            step = solutionSubmissionItem.step.id
        )
        screenManager.continueCourse(this, step)
    }

    private fun handleSubmissionCheckBoxClick(solutionSubmissionItem: SolutionItem.SubmissionItem) {
        selectionHelper.toggle(solutionsAdapter.items.indexOf(solutionSubmissionItem))
        val lessonId = solutionSubmissionItem.lesson.id
        val sectionId = solutionSubmissionItem.section.id

        val lessonIndex = solutionsAdapter.items.indexOfFirst { item ->
            item is SolutionItem.LessonItem && item.lesson.id == lessonId
        }

        val sectionIndex = solutionsAdapter.items.indexOfFirst { item ->
            item is SolutionItem.SectionItem && item.section.id == sectionId
        }

        if (isAllSubmissionsSelectedInLesson(lessonIndex)) {
            selectionHelper.select(lessonIndex)
        } else {
            selectionHelper.deselect(lessonIndex)
        }

        if (isAllLessonsSelectedInSection(sectionIndex, sectionId)) {
            selectionHelper.select(sectionIndex)
        } else {
            selectionHelper.deselect(sectionIndex)
        }
        invalidateOptionsMenu()
    }

    /**
     *  Selectable submissions functions
     */

    private fun fetchSelectedSubmissionItems(): List<SolutionItem.SubmissionItem> =
        solutionsAdapter.items
            .filterIndexed { index, _ -> selectionHelper.isSelected(index) }
            .filterIsInstance<SolutionItem.SubmissionItem>()
            .filter { it.submission.status == Submission.Status.LOCAL }
}