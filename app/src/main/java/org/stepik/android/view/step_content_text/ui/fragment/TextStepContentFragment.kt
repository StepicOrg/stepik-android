package org.stepik.android.view.step_content_text.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepic.droid.util.getStepType
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_content_text.model.FontSize
import org.stepik.android.model.Step
import org.stepik.android.presentation.step_content_text.TextStepContentPresenter
import org.stepik.android.presentation.step_content_text.TextStepContentView
import org.stepik.android.view.step_source.ui.dialog.EditStepSourceDialogFragment
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class TextStepContentFragment :
    Fragment(),
    TextStepContentView,
    EditStepSourceDialogFragment.Callback {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            TextStepContentFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var stepWrapper: StepPersistentWrapper

    @Inject
    lateinit var lessonData: LessonData

    private var stepId: Long by argument()

    private lateinit var presenter: TextStepContentPresenter

    private var latexLayout: LatexSupportableEnhancedFrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        injectComponent()

        presenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(TextStepContentPresenter::class.java)
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepId)
            .textStepContentComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        latexLayout ?: inflater.inflate(R.layout.step_text_header, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (latexLayout == null) {
            latexLayout = view as LatexSupportableEnhancedFrameLayout

            invalidateText()
        }
    }

    private fun invalidateText() {
        val text = stepWrapper
            .step
            .block
            ?.text
            ?.takeIf(String::isNotEmpty)

        val view = latexLayout ?: return

        view.isVisible = text != null
        presenter.onSetTextContentSize()
        if (text != null) {
            view.setText(text)
            view.setTextIsSelectable(true)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onPause() {
        setHasOptionsMenu(false)
        super.onPause()
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.step_edit_menu, menu)
        menu.findItem(R.id.menu_item_edit)?.isVisible =
            lessonData.lesson.actions?.editLesson != null
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_item_edit -> {
                showStepEditDialog()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }

    private fun showStepEditDialog() {
        reportStepAction(Analytic.Steps.STEP_EDIT_OPENED, AmplitudeAnalytic.Steps.STEP_EDIT_OPENED, stepWrapper.step)

        EditStepSourceDialogFragment
            .newInstance(stepWrapper, lessonData.lesson.title.orEmpty())
            .showIfNotExists(childFragmentManager, EditStepSourceDialogFragment.TAG)
    }

    override fun onDestroy() {
        latexLayout = null
        super.onDestroy()
    }

    override fun setTextContentFontSize(fontSize: FontSize) {
        latexLayout?.setTextSize(fontSize.size) ?: return
    }

    override fun onStepContentChanged(stepWrapper: StepPersistentWrapper) {
        this.stepWrapper = stepWrapper
        reportStepAction(Analytic.Steps.STEP_EDIT_COMPLETED, AmplitudeAnalytic.Steps.STEP_EDIT_COMPLETED, stepWrapper.step)
        invalidateText()
    }

    private fun reportStepAction(eventName: String, amplitudeEventName: String, step: Step) {
        analytic.reportEventWithName(eventName, step.getStepType())
        analytic.reportAmplitudeEvent(
            amplitudeEventName, mapOf(
                AmplitudeAnalytic.Steps.Params.TYPE to step.getStepType(),
                AmplitudeAnalytic.Steps.Params.NUMBER to step.position,
                AmplitudeAnalytic.Steps.Params.STEP to step.id
            ))
    }
}