package org.stepic.droid.persistence.model

import org.stepik.android.model.Step
import org.stepik.android.model.Video

data class StepPersistentWrapper(
        val step: Step,
        val cachedVideo: Video? = null // maybe more abstract
)