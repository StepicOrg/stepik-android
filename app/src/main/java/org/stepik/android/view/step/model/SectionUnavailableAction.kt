package org.stepik.android.view.step.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.view.course_content.model.RequiredSection
import java.util.Date

sealed class SectionUnavailableAction : Parcelable {
    @Parcelize
    data class RequiresSection(
        val currentSection: Section,
        val targetSection: Section,
        val requiredSection: RequiredSection
    ) : SectionUnavailableAction()

    @Parcelize
    data class RequiresExam(
        val currentSection: Section,
        val targetSection: Section,
        val requiredSection: RequiredSection?
    ) : SectionUnavailableAction()

    @Parcelize
    data class RequiresDate(
        val currentSection: Section,
        val nextLesson: Lesson,
        val date: Date
    ) : SectionUnavailableAction()
}