package org.stepik.android.view.step_quiz_code.ui.delegate

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.content.res.AppCompatResources
import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.*
import org.stepic.droid.R
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.adapters.CodeToolbarAdapter

class CodeLayoutDelegate(
    codeContainerView: View,
    private val stepWrapper: StepPersistentWrapper,
    private val codeTemplates: Map<String, String>,
    private val codeQuizInstructionDelegate: CodeQuizInstructionDelegate,
    private var codeToolbarAdapter: CodeToolbarAdapter?,
    private val onChangeLanguageClicked: () -> Unit
) {

    private val codeLayout = codeContainerView.codeStepLayout
    private val stepQuizActionChangeLang = codeContainerView.stepQuizActionChangeLang

    init {
        /**
         * Actions
         */

        if (codeTemplates.size > 1) {
            val stepQuizActionChangeLangDrawable = AppCompatResources
                .getDrawable(stepQuizActionChangeLang.context, R.drawable.ic_arrow_bottom)
                ?.mutate()
                ?.let(DrawableCompat::wrap)
                ?.also {
                    DrawableCompat
                        .setTintList(it, ContextCompat.getColorStateList(stepQuizActionChangeLang.context, R.color.color_step_quiz_code_lang_arrow))
                }

            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                stepQuizActionChangeLang, null, null, stepQuizActionChangeLangDrawable, null)

            stepQuizActionChangeLang.setOnClickListener { onChangeLanguageClicked() }
        }
    }

    /**
     * if [code] is null then default code template for [lang] will be used
     */
    fun setLanguage(lang: String, code: String? = null) {
        codeLayout.lang = extensionForLanguage(lang)
        stepQuizActionChangeLang.text = lang
        codeLayout.setText(code ?: codeTemplates[lang] ?: "")
        codeToolbarAdapter?.setLanguage(lang)
    }

    fun setDetailsContentData(lang: String?) {
        codeQuizInstructionDelegate.setCodeDetailsData(stepWrapper.step, lang)
    }

    fun setEnabled(isEnabled: Boolean) {
        codeLayout.isEnabled = isEnabled
        stepQuizActionChangeLang.isEnabled = isEnabled
    }
}