package org.stepik.android.view.step.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import java.util.Date

sealed class SectionUnavailableAction : Parcelable {
    @Parcelize
    data class RequiresSection(
        val currentSection: Section,
        val targetSection: Section,
        val requiredSection: Section?,
        val requiredProgress: Progress?
    ) : SectionUnavailableAction()

    @Parcelize
    data class RequiresExam(
        val currentSection: Section,
        val targetSection: Section
    ) : SectionUnavailableAction()

    @Parcelize
    data class RequiresDate(
        val currentSection: Section,
        val nextLesson: Lesson,
        val date: Date
    ) : SectionUnavailableAction()
}

