package org.stepik.android.view.debug.ui

import androidx.fragment.app.Fragment
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.model.Block
import org.stepik.android.model.Lesson
import org.stepik.android.model.Step
import org.stepik.android.view.injection.step.StepComponent
import org.stepik.android.view.step_source.ui.dialog.EditStepSourceDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

object EditStepSourcePreviewData {
    private const val PREVIEW_STEP_ID = -1L
    private const val PREVIEW_LESSON_TITLE = "Sample lesson"

    // ComponentManager keeps step components weakly; retain this one while the preview can be reopened.
    private var previewStepComponent: StepComponent? = null

    fun show(host: Fragment) {
        if (host.childFragmentManager.findFragmentByTag(EditStepSourceDialogFragment.TAG) != null) return

        val step = Step(
            id = PREVIEW_STEP_ID,
            lesson = -1L,
            position = 1L,
            block = Block(name = AppConstants.TYPE_TEXT, text = "Sample step text for the editor.")
        )
        val stepWrapper = StepPersistentWrapper(step)
        previewStepComponent = App.componentManager().stepParentComponent(
            stepWrapper,
            LessonData(
                lesson = Lesson(id = step.lesson, title = PREVIEW_LESSON_TITLE),
                unit = null,
                section = null,
                course = null
            )
        )
        EditStepSourceDialogFragment
            .newInstance(stepWrapper, lessonTitle = PREVIEW_LESSON_TITLE)
            .showIfNotExists(host.childFragmentManager, EditStepSourceDialogFragment.TAG)
    }
}
