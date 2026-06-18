package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import com.google.android.material.button.MaterialButton
import org.stepic.droid.R
import org.stepic.droid.code.ui.CodeEditorLayout
import org.stepic.droid.databinding.LayoutStepQuizCodeBinding
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepik.android.model.Step

class CodeLayoutDelegate private constructor(
    private val codeLayout: CodeEditorLayout,
    private val stepQuizActionChangeLang: MaterialButton,
    private val step: Step,
    private val codeTemplates: Map<String, String>,
    private val codeQuizInstructionDelegate: CodeQuizInstructionDelegate,
    private var codeToolbarAdapter: CodeToolbarAdapter?,
    private val onChangeLanguageClicked: () -> Unit
) {
    constructor(
        codeLayoutBinding: LayoutStepQuizCodeBinding,
        step: Step,
        codeTemplates: Map<String, String>,
        codeQuizInstructionDelegate: CodeQuizInstructionDelegate,
        codeToolbarAdapter: CodeToolbarAdapter?,
        onChangeLanguageClicked: () -> Unit
    ) : this(
        codeLayoutBinding.codeStepLayout,
        codeLayoutBinding.stepQuizActionChangeLang,
        step,
        codeTemplates,
        codeQuizInstructionDelegate,
        codeToolbarAdapter,
        onChangeLanguageClicked
    )

    constructor(
        codeContainerView: View,
        step: Step,
        codeTemplates: Map<String, String>,
        codeQuizInstructionDelegate: CodeQuizInstructionDelegate,
        codeToolbarAdapter: CodeToolbarAdapter?,
        onChangeLanguageClicked: () -> Unit
    ) : this(
        codeContainerView.findViewById(R.id.codeStepLayout),
        codeContainerView.findViewById(R.id.stepQuizActionChangeLang),
        step,
        codeTemplates,
        codeQuizInstructionDelegate,
        codeToolbarAdapter,
        onChangeLanguageClicked
    )

    init {
        /**
         * Actions
         */

        if (codeTemplates.size > 1) {
            stepQuizActionChangeLang.setIconResource(R.drawable.ic_arrow_bottom)
            stepQuizActionChangeLang.setOnClickListener { onChangeLanguageClicked() }
        } else {
            stepQuizActionChangeLang.setIconResource(0)
        }
    }

    /**
     * if [code] is null then default code template for [lang] will be used
     */
    fun setLanguage(lang: String, code: String? = null) {
        codeLayout.lang = extensionForLanguage(lang)
        stepQuizActionChangeLang.text = lang
        codeLayout.setTextIfChanged(code ?: codeTemplates[lang] ?: "")
        codeToolbarAdapter?.setLanguage(lang)
    }

    fun setDetailsContentData(lang: String?) {
        codeQuizInstructionDelegate.setCodeDetailsData(step, lang)
    }

    fun setEnabled(isEnabled: Boolean) {
        codeLayout.isEnabled = isEnabled
        stepQuizActionChangeLang.isEnabled = isEnabled
    }
}
