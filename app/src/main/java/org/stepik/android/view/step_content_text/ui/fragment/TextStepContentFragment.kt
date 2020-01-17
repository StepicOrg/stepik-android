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
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.analytic.reportStepEvent
import org.stepik.android.domain.step_content_text.model.FontSize
import org.stepik.android.presentation.step_content_text.TextStepContentPresenter
import org.stepik.android.presentation.step_content_text.TextStepContentView
import org.stepik.android.view.latex.ui.delegate.LatexViewDelegate
import org.stepik.android.view.latex.ui.widget.LatexView
import org.stepik.android.view.latex.ui.widget.LatexWebView
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

    @Inject
    lateinit var latexViewDelegate: LatexViewDelegate

    private var stepId: Long by argument()

    private lateinit var presenter: TextStepContentPresenter

    private var latexView: LatexView? = null
    private var latexWebView: LatexWebView? = null

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
        inflater
            .inflate(R.layout.step_text_header, container, false)
            .also {
                it as ViewGroup

                if (latexWebView == null) {
                    latexWebView = LayoutInflater
                        .from(requireContext().applicationContext)
                        .inflate(R.layout.layout_latex_webview, it, false) as LatexWebView
                }

                latexWebView?.let(it::addView)
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (latexView == null) {
            latexView = view as LatexView

            invalidateText()
        }
    }

    private fun invalidateText() {
        val text = stepWrapper
            .step
            .block
            ?.text
            ?.takeIf(String::isNotEmpty)

        val view = latexView ?: return

        view.isVisible = text != null
        latexViewDelegate.setText(text)
        presenter.onSetTextContentSize()
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
        latexViewDelegate.attach(requireContext(), latexView ?: return)
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
        latexViewDelegate.detach()
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
        analytic.reportStepEvent(Analytic.Steps.STEP_EDIT_OPENED, AmplitudeAnalytic.Steps.STEP_EDIT_OPENED, stepWrapper.step)

        EditStepSourceDialogFragment
            .newInstance(stepWrapper, lessonData.lesson.title.orEmpty())
            .showIfNotExists(childFragmentManager, EditStepSourceDialogFragment.TAG)
    }

    override fun onDestroyView() {
        (view as? ViewGroup)?.removeView(latexWebView)
        super.onDestroyView()
    }

    override fun onDestroy() {
        latexWebView = null
        super.onDestroy()
    }

    override fun setTextContentFontSize(fontSize: FontSize) {
        val latexView = latexView ?: return
        latexView.attributes = latexView.attributes.copy(textSize = fontSize.size)
    }

    override fun onStepContentChanged(stepWrapper: StepPersistentWrapper) {
        this.stepWrapper = stepWrapper
        analytic.reportStepEvent(Analytic.Steps.STEP_EDIT_COMPLETED, AmplitudeAnalytic.Steps.STEP_EDIT_COMPLETED, stepWrapper.step)
        invalidateText()
    }
}