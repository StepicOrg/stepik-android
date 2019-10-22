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
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_content_text.model.FontSize
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
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            TextStepContentFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                    this.lessonData = lessonData
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: TextStepContentPresenter

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonData: LessonData by argument()

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
        App.component()
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
        EditStepSourceDialogFragment
            .newInstance(stepWrapper, lessonData)
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
        invalidateText()
    }
}