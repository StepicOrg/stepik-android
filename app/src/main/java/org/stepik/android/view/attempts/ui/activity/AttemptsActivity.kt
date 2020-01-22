package org.stepik.android.view.attempts.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_attempts.*
import kotlinx.android.synthetic.main.empty_default.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.presentation.attempts.AttemptsPresenter
import org.stepik.android.presentation.attempts.AttemptsView
import org.stepik.android.view.attempts.model.AttemptCacheItem
import org.stepik.android.view.attempts.ui.adapter.delegate.AttemptLessonAdapterDelegate
import org.stepik.android.view.attempts.ui.adapter.delegate.AttemptSectionAdapterDelegate
import org.stepik.android.view.attempts.ui.adapter.delegate.AttemptSubmissionAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import javax.inject.Inject

class AttemptsActivity : FragmentActivityBase(), AttemptsView {
    companion object {
        private const val EXTRA_COURSE_ID = "course_id"
        fun createIntent(context: Context, courseId: Long): Intent =
            Intent(context, AttemptsActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var attemptsPresenter: AttemptsPresenter

    private var attemptsAdapter: DefaultDelegateAdapter<AttemptCacheItem> = DefaultDelegateAdapter()

    private val viewStateDelegate =
        ViewStateDelegate<AttemptsView.State>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempts)

        injectComponent()
        attemptsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(AttemptsPresenter::class.java)
        initCenteredToolbar(R.string.attempts_toolbar_title, showHomeButton = true)
        attemptsFeedback.setCompoundDrawables(start = R.drawable.ic_step_quiz_validation)

        attemptsAdapter += AttemptSectionAdapterDelegate()
        attemptsAdapter += AttemptLessonAdapterDelegate()
        attemptsAdapter += AttemptSubmissionAdapterDelegate()

        with(attemptsRecycler) {
            adapter = attemptsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(DividerItemDecoration(this@AttemptsActivity, LinearLayoutManager.VERTICAL).apply {
                ContextCompat.getDrawable(this@AttemptsActivity, R.drawable.list_divider_h)?.let(::setDrawable)
            })
        }
        initViewStateDelegate()
        attemptsPresenter.fetchAttemptCacheItems()
    }

    private fun injectComponent() {
        App.component()
            .attemptsComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        attemptsPresenter.attachView(this)
    }

    override fun onStop() {
        attemptsPresenter.detachView(this)
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
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
        viewStateDelegate.addState<AttemptsView.State.AttemptsLoaded>(attemptsContainer, attemptsFeedback, attemptsFeedbackSeparator, attemptsRecycler)
    }

    override fun setState(state: AttemptsView.State) {
        viewStateDelegate.switchState(state)
        if (state is AttemptsView.State.AttemptsLoaded) {
            attemptsAdapter.items = state.attempts
        }
    }
}