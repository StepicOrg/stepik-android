package org.stepik.android.view.step_content_text.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument
import org.stepik.android.presentation.step_content_text.TextStepContentPresenter
import org.stepik.android.presentation.step_content_text.TextStepContentView
import javax.inject.Inject

class TextStepContentFragment : Fragment(), TextStepContentView {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper): Fragment =
            TextStepContentFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: TextStepContentPresenter

    private var stepWrapper: StepPersistentWrapper by argument()
    private var latexLayout: LatexSupportableEnhancedFrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        presenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(TextStepContentPresenter::class.java)

        retainInstance = true
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

            val text = stepWrapper
                .step
                .block
                ?.text
                ?.takeIf(String::isNotEmpty)

            view.changeVisibility(needShow = text != null)
            presenter.onSetTextContentSize()
            if (text != null) {
                view.setText(text)
                view.setTextIsSelectable(true)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun onDestroy() {
        latexLayout = null
        super.onDestroy()
    }

    override fun setTextContentFontSize(fontSize: Float) {
        latexLayout?.setTextSize(fontSize) ?: return
    }
}