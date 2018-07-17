package org.stepic.droid.web

import org.stepic.droid.model.Lesson
import org.stepik.android.model.Meta

class LessonStepicResponse(
        meta: Meta,
        val lessons: List<Lesson>? = null
) : MetaResponseBase(meta)
