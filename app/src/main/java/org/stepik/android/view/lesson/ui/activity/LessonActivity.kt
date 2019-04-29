package org.stepik.android.view.lesson.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_lesson.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.lesson.LessonPresenter
import org.stepik.android.presentation.lesson.LessonView
import org.stepik.android.view.lesson.ui.delegate.LessonInfoTooltipDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class LessonActivity : FragmentActivityBase(), LessonView {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var lessonPresenter: LessonPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<LessonView.State>
    private lateinit var viewStepStateDelegate: ViewStateDelegate<LessonView.StepsState>

    private var infoMenuItem: MenuItem? = null
    private var isInfoMenuItemVisible: Boolean = false
        set(value) {
            field = value
            infoMenuItem?.isVisible = value
        }

    private lateinit var lessonInfoTooltipDelegate: LessonInfoTooltipDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)

        injectComponent()
        lessonPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(LessonPresenter::class.java)

        initCenteredToolbar(R.string.lesson_title, showHomeButton = true)

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<LessonView.State.Idle>(lessonPlaceholder)
        viewStateDelegate.addState<LessonView.State.Loading>(lessonPlaceholder)
        viewStateDelegate.addState<LessonView.State.LessonNotFound>(lessonNotFound)
        viewStateDelegate.addState<LessonView.State.EmptyLogin>(emptyLogin)
        viewStateDelegate.addState<LessonView.State.NetworkError>(errorNoConnection)
        viewStateDelegate.addState<LessonView.State.EmptyLesson>(emptyLesson)
        viewStateDelegate.addState<LessonView.State.LessonLoaded>(lessonPager)

        viewStepStateDelegate = ViewStateDelegate()
        viewStepStateDelegate.addState<LessonView.StepsState.Idle>(lessonPlaceholder)
        viewStepStateDelegate.addState<LessonView.StepsState.Loading>(lessonPlaceholder)
        viewStepStateDelegate.addState<LessonView.StepsState.NetworkError>(errorNoConnection)
        viewStepStateDelegate.addState<LessonView.StepsState.Loaded>(lessonPager)

        lessonInfoTooltipDelegate = LessonInfoTooltipDelegate(centeredToolbar)
    }

    private fun injectComponent() {
        App.component()
            .lessonComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        lessonPresenter.attachView(this)
    }

    override fun onStop() {
        lessonPresenter.detachView(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.lesson_menu, menu)
        infoMenuItem = menu.findItem(R.id.lesson_menu_item_info)
        infoMenuItem?.isVisible = isInfoMenuItemVisible
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.lesson_menu_item_info -> {
                lessonPresenter.onShowLessonInfoClicked(lessonPager.currentItem)
                true
            }
             else ->
                super.onOptionsItemSelected(item)
        }

    override fun setState(state: LessonView.State) {
        viewStateDelegate.switchState(state)
        when (state) {
            is LessonView.State.LessonLoaded ->
                viewStepStateDelegate.switchState(state.stepsState)
        }

        isInfoMenuItemVisible =
            state is LessonView.State.LessonLoaded &&
            state.stepsState is LessonView.StepsState.Loaded
    }

    override fun showLessonInfoTooltip(stepWorth: Long, lessonTimeToComplete: Long, certificateThreshold: Long) {
        lessonInfoTooltipDelegate
            .showLessonInfoTooltip(stepWorth, lessonTimeToComplete, certificateThreshold)
    }
}