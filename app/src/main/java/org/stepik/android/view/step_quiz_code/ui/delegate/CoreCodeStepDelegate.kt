package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.*
import org.stepic.droid.R
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState

class CoreCodeStepDelegate(
    codeContainerView: View,
    private val stepWrapper: StepPersistentWrapper,
    private val codeQuizInstructionDelegate: CodeQuizInstructionDelegate,
    private val actionsListener: ActionsListener,
    private var codeToolbarAdapter: CodeToolbarAdapter?
) {

    private val codeLayout = codeContainerView.codeStepLayout
    private val stepQuizActionChangeLang = codeContainerView.stepQuizActionChangeLang

    val codeOptions = stepWrapper.step.block?.options ?: throw IllegalArgumentException("Code options shouldn't be null")

    init {
        /**
         * Actions
         */
        stepQuizActionChangeLang.setOnClickListener { actionsListener.onChangeLanguageClicked() }
        stepQuizActionChangeLang.setCompoundDrawables(end = R.drawable.ic_arrow_bottom)
    }

    /**
     * if [code] is null then default code template for [lang] will be used
     */
    fun setLanguage(lang: String, code: String? = null) {
        codeLayout.lang = extensionForLanguage(lang)
        stepQuizActionChangeLang.text = lang
        codeLayout.setText(code ?: resetCode(lang))
        codeToolbarAdapter?.setLanguage(lang)
    }

    fun setDetailsContentData(lang: String?) {
        codeQuizInstructionDelegate.setCodeDetailsData(stepWrapper.step, lang)
    }

    fun onFullscreenClicked(lang: String, code: String) {
        actionsListener.onFullscreenClicked(lang, code)
    }

    fun onLanguageSelected(lang: String): CodeStepQuizFormState.Lang =
        CodeStepQuizFormState.Lang(lang, codeOptions.codeTemplates[lang] ?: "")

    fun resetCode(lang: String): String =
        codeOptions.codeTemplates[lang] ?: ""

    fun setEnabled(isEnabled: Boolean) {
        codeLayout.isEnabled = isEnabled
        stepQuizActionChangeLang.isEnabled = isEnabled
    }

    interface ActionsListener {
        fun onChangeLanguageClicked()
        fun onFullscreenClicked(lang: String, code: String)
    }
}