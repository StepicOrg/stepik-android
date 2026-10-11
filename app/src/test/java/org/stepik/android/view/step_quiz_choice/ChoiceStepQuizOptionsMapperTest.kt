package org.stepik.android.view.step_quiz_choice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.stepik.android.model.Submission
import org.stepik.android.view.step_quiz_choice.mapper.ChoiceStepQuizOptionsMapper

class ChoiceStepQuizOptionsMapperTest {
    private val mapper = ChoiceStepQuizOptionsMapper()

    private val duplicateOptions =
        listOf(
            "correct answer",
            "wrong answer",
            "wrong answer",
            "wrong answer"
        )

    @Test
    fun `duplicate options should keep distinct ui identities`() {
        val choices = mapper.mapChoices(
            options = duplicateOptions,
            choices = null,
            submission = null,
            isQuizEnabled = true
        )

        assertEquals(choices.size, choices.map { it.id }.toSet().size)
    }

    @Test
    fun `duplicate option ids should match option slots`() {
        val choices = mapper.mapChoices(
            options = duplicateOptions,
            choices = null,
            submission = null,
            isQuizEnabled = true
        )

        assertEquals(listOf(0, 1, 2, 3), choices.map { it.id })
    }

    @Test
    fun `duplicate option ids should be stable after repeated mapping`() {
        val firstMapping = mapper.mapChoices(
            options = duplicateOptions,
            choices = null,
            submission = null,
            isQuizEnabled = true
        )

        val secondMapping = mapper.mapChoices(
            options = duplicateOptions,
            choices = listOf(false, false, true, false),
            submission = Submission(status = Submission.Status.CORRECT),
            isQuizEnabled = false
        )

        assertEquals(firstMapping.map { it.id }, secondMapping.map { it.id })
    }

    @Test
    fun `duplicate option click should resolve to clicked slot`() {
        val choices = mapper.mapChoices(
            options = duplicateOptions,
            choices = null,
            submission = null,
            isQuizEnabled = true
        )

        assertEquals(2, choices.indexOf(choices[2]))
    }

    @Test
    fun `duplicate options should preserve submitted choices by slot`() {
        val choices = mapper.mapChoices(
            options = duplicateOptions,
            choices = listOf(false, false, true, false),
            submission = Submission(status = Submission.Status.CORRECT),
            isQuizEnabled = false
        )

        assertNull(choices[1].correct)
        assertEquals(true, choices[2].correct)
        assertNull(choices[3].correct)
    }
}
