package org.stepic.droid.events

import org.stepic.droid.model.Progress

data class UpdateSectionProgressEvent(val sectionId: Long, val progress: Progress, val courseId : Long)
