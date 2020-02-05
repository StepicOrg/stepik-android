package org.stepik.android.view.attempts.ui.activity

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
import kotlinx.android.synthetic.main.activity_attempts.*
import kotlinx.android.synthetic.main.empty_default.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.attempts.model.AttemptCacheItem
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Submission
import org.stepik.android.presentation.attempts.AttemptsPresenter
import org.stepik.android.presentation.attempts.AttemptsView
import org.stepik.android.view.attempts.ui.adapter.delegate.AttemptLessonAdapterDelegate
import org.stepik.android.view.attempts.ui.adapter.delegate.AttemptSectionAdapterDelegate
import org.stepik.android.view.attempts.ui.adapter.delegate.AttemptSubmissionAdapterDelegate
import org.stepik.android.view.attempts.ui.dialog.RemoveCachedAttemptsDialog
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.ui.adapters.selection.MultipleChoiceSelectionHelper
import ru.nobird.android.view.base.ui.extension.getDrawableCompat
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import java.util.ArrayList
import javax.inject.Inject

class AttemptsActivity : FragmentActivityBase(), AttemptsView, RemoveCachedAttemptsDialog.Callback {
    companion object {
        private const val EVALUATION_FRAME_DURATION_MS = 250
        private const val CHECKED_ITEMS_ARGUMENT = "checked_items"

        private const val EXTRA_COURSE_ID = "course_id"

        fun createIntent(context: Context, courseId: Long): Intent =
            Intent(context, AttemptsActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var attemptsPresenter: AttemptsPresenter

    private var isDeleteMenuItemVisible = false

    private var attemptsAdapter: DefaultDelegateAdapter<AttemptCacheItem> = DefaultDelegateAdapter()
    private val selectionHelper = MultipleChoiceSelectionHelper(attemptsAdapter)
    private val checkedIndices = ArrayList<Int>()

    private val viewStateDelegate =
        ViewStateDelegate<AttemptsView.State>()

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempts)

        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1)
        injectComponent(courseId)

        attemptsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(AttemptsPresenter::class.java)
        initCenteredToolbar(R.string.attempts_toolbar_title, showHomeButton = true)
        attemptsFeedback.setCompoundDrawables(start = R.drawable.ic_step_quiz_validation)

        attemptsAdapter += AttemptSectionAdapterDelegate(selectionHelper, onClick = ::handleSectionCheckboxClick)
        attemptsAdapter += AttemptLessonAdapterDelegate(selectionHelper, onClick = ::handleLessonCheckboxClick)
        attemptsAdapter += AttemptSubmissionAdapterDelegate(
            selectionHelper,
            onCheckboxClick = ::handleSubmissionCheckBoxClick,
            onItemClick = ::handleSubmissionItemClick
        )

        with(attemptsRecycler) {
            itemAnimator = null
            adapter = attemptsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(DividerItemDecoration(this@AttemptsActivity, LinearLayoutManager.VERTICAL).apply {
                ContextCompat.getDrawable(this@AttemptsActivity, R.drawable.list_divider_h)?.let(::setDrawable)
            })
        }

        val evaluationDrawable = AnimationDrawable()
        evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.addFrame(getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.isOneShot = false

        attemptsSubmitFeedback.setCompoundDrawablesWithIntrinsicBounds(evaluationDrawable, null, null, null)
        evaluationDrawable.start()

        attemptsSubmitButton.setOnClickListener {
            if (fetchSelectedSubmissionItems().isEmpty()) {
                for (index in attemptsAdapter.items.indices) {
                    selectionHelper.select(index)
                }
            }
            val selectedSubmissions = fetchSelectedSubmissionItems().map { it.submission }
            attemptsPresenter.submitSolutions(selectedSubmissions)
        }

        initViewStateDelegate()
        attemptsPresenter.fetchAttemptCacheItems(localOnly = true)

        if (savedInstanceState != null && savedInstanceState.containsKey(CHECKED_ITEMS_ARGUMENT)) {
            checkedIndices.addAll(savedInstanceState.getIntegerArrayList(CHECKED_ITEMS_ARGUMENT) as ArrayList<Int>)
        }
    }

    private fun injectComponent(courseId: Long) {
        App.component()
            .attemptsComponentBuilder()
            .courseId(courseId)
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        attemptsPresenter.attachView(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val items = attemptsAdapter.items.withIndex().filter { selectionHelper.isSelected(it.index) }.map { it.index }
        outState.putIntegerArrayList(CHECKED_ITEMS_ARGUMENT, items as ArrayList<Int>)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        attemptsPresenter.detachView(this)
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
            centeredToolbarTitle.text = getString(R.string.attempts_toolbar_title)
            attemptsSubmitButton.text = getString(R.string.attempts_submit_all)
        } else {
            centeredToolbarTitle.text = getString(R.string.attempts_selected, selectedCount)
            attemptsSubmitButton.text = resources.getQuantityString(R.plurals.submit_solutions, selectedCount, selectedCount)
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
        viewStateDelegate.addState<AttemptsView.State.Idle>()
        viewStateDelegate.addState<AttemptsView.State.Loading>(loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<AttemptsView.State.Empty>(report_empty)
        viewStateDelegate.addState<AttemptsView.State.Error>()
        viewStateDelegate.addState<AttemptsView.State.AttemptsLoaded>(
            attemptsContainer,
            attemptsFeedback,
            attemptsFeedbackSeparator,
            attemptsRecycler,
            attemptsSubmissionSeparator,
            attemptsSubmitButton
        )
    }

    override fun setState(state: AttemptsView.State) {
        viewStateDelegate.switchState(state)
        isDeleteMenuItemVisible =
            (state as? AttemptsView.State.AttemptsLoaded)?.isSending == false

        if (state is AttemptsView.State.AttemptsLoaded) {
            attemptsAdapter.items = state.attempts
            attemptsSubmitButton.isEnabled = !state.isSending
            attemptsSubmitFeedback.isVisible = state.isSending
            if (checkedIndices.isNotEmpty()) {
                checkedIndices.forEach { selectionHelper.select(it) }
            }
        }

        invalidateOptionsMenu()
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun onAttemptRemoveConfirmed(attemptIds: List<Long>) {
        attemptsPresenter.removeAttempts(attemptIds)
        checkedIndices.clear()
        selectionHelper.reset()
        invalidateOptionsMenu()
    }

    override fun onFinishedSending() {
        selectionHelper.reset()
        invalidateOptionsMenu()
    }

    private fun showRemoveAttemptsDialog(attemptIds: List<Long>) {
        RemoveCachedAttemptsDialog
            .newInstance(attemptIds = attemptIds)
            .showIfNotExists(supportFragmentManager, RemoveCachedAttemptsDialog.TAG)
    }

    /**
     *  isSelected functions
     */

    private fun isAllSubmissionsSelectedInLesson(lessonIndex: Int): Boolean {
        var areAllSelectedInLesson = true
        for (index in lessonIndex + 1 until attemptsAdapter.items.size) {
            val item = attemptsAdapter.items[index]
            if (item is AttemptCacheItem.SubmissionItem) {
                areAllSelectedInLesson = areAllSelectedInLesson && selectionHelper.isSelected(index)
            } else {
                break
            }
        }
        return areAllSelectedInLesson
    }

    private fun isAllLessonsSelectedInSection(sectionIndex: Int, sectionId: Long): Boolean {
        var areAllSelectedInSection = true
        for (index in sectionIndex + 1 until attemptsAdapter.items.size) {
            val item = attemptsAdapter.items[index]
            if (item is AttemptCacheItem.LessonItem) {
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

    private fun handleSectionCheckboxClick(attemptCacheSectionItem: AttemptCacheItem.SectionItem) {
        val itemIndex = attemptsAdapter.items.indexOf(attemptCacheSectionItem)
        val sectionId = attemptCacheSectionItem.section.id
        selectionHelper.toggle(itemIndex)
        val isSelected = selectionHelper.isSelected(itemIndex)

        for (index in itemIndex + 1 until attemptsAdapter.items.size) {
            val itemSectionId = when (val item = attemptsAdapter.items[index]) {
                is AttemptCacheItem.LessonItem ->
                    item.section.id
                is AttemptCacheItem.SubmissionItem ->
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

    private fun handleLessonCheckboxClick(attemptCacheLessonItem: AttemptCacheItem.LessonItem) {
        val itemIndex = attemptsAdapter.items.indexOf(attemptCacheLessonItem)
        val lessonId = attemptCacheLessonItem.lesson.id
        val sectionId = attemptCacheLessonItem.section.id
        selectionHelper.toggle(itemIndex)
        val isSelected = selectionHelper.isSelected(itemIndex)

        for (index in itemIndex + 1 until attemptsAdapter.items.size) {
            val item = attemptsAdapter.items[index]
            if (item is AttemptCacheItem.SubmissionItem && item.lesson.id == lessonId) {
                    if (isSelected) {
                        selectionHelper.select(index)
                    } else {
                        selectionHelper.deselect(index)
                    }
                } else {
                break
            }
        }

        val sectionIndex = attemptsAdapter.items.indexOfFirst { item ->
            item is AttemptCacheItem.SectionItem && item.section.id == sectionId
        }

        if (isAllLessonsSelectedInSection(sectionIndex, sectionId)) {
            selectionHelper.select(sectionIndex)
        } else {
            selectionHelper.deselect(sectionIndex)
        }
        invalidateOptionsMenu()
    }

    private fun handleSubmissionItemClick(attemptCacheSubmissionItem: AttemptCacheItem.SubmissionItem) {
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.LocalSubmissions.LOCAL_SUBMISSION_ITEM_CLICKED, mapOf(
                AmplitudeAnalytic.LocalSubmissions.Params.STEP_ID to attemptCacheSubmissionItem.step.id
            )
        )
        val step = LastStep(
            id = "",
            unit = attemptCacheSubmissionItem.unit.id,
            lesson = attemptCacheSubmissionItem.lesson.id,
            step = attemptCacheSubmissionItem.step.id
        )
        screenManager.continueCourse(this, step)
    }

    private fun handleSubmissionCheckBoxClick(attemptCacheSubmissionItem: AttemptCacheItem.SubmissionItem) {
        selectionHelper.toggle(attemptsAdapter.items.indexOf(attemptCacheSubmissionItem))
        val lessonId = attemptCacheSubmissionItem.lesson.id
        val sectionId = attemptCacheSubmissionItem.section.id

        val lessonIndex = attemptsAdapter.items.indexOfFirst { item ->
            item is AttemptCacheItem.LessonItem && item.lesson.id == lessonId
        }

        val sectionIndex = attemptsAdapter.items.indexOfFirst { item ->
            item is AttemptCacheItem.SectionItem && item.section.id == sectionId
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

    private fun fetchSelectedSubmissionItems(): List<AttemptCacheItem.SubmissionItem> =
        attemptsAdapter.items
            .filterIndexed { index, _ -> selectionHelper.isSelected(index) }
            .filterIsInstance<AttemptCacheItem.SubmissionItem>()
            .filter { it.submission.status == Submission.Status.LOCAL }
}