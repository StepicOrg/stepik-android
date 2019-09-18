package org.stepik.android.view.course_content.model

import org.stepik.android.model.Progress
import org.stepik.android.model.Section

data class RequiredSection(
    val section: Section,
    val progress: Progress
)