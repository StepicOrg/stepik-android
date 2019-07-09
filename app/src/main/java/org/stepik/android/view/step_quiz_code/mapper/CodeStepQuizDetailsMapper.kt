package org.stepik.android.view.step_quiz_code.mapper

import org.stepik.android.model.Step
import org.stepik.android.model.code.CodeLimit
import org.stepik.android.view.step_quiz_code.model.CodeDetail

class CodeStepQuizDetailsMapper {
    fun mapToCodeDetails(step: Step, lang: String?): List<CodeDetail> {
        val options = step.block?.options
            ?: return emptyList()

        val samples = options
            .samples
            .mapIndexed { i, sample -> CodeDetail.Sample(i + 1, sample[0].trim('\n'), sample[1].trim('\n')) }

        val codeLimit = options.limits[lang]
            ?: CodeLimit(options.executionTimeLimit, options.executionMemoryLimit)

        val limits =
            listOf(
                CodeDetail.Limit(codeLimit.time, CodeDetail.Limit.Type.TIME),
                CodeDetail.Limit(codeLimit.memory, CodeDetail.Limit.Type.MEMORY)
            )

        return samples + limits
    }
}