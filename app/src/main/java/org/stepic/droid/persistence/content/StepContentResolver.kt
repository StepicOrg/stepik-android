package org.stepic.droid.persistence.content

import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepik.android.model.Step

interface StepContentResolver {
    fun getDownloadableContentFromStep(step: Step, configuration: DownloadConfiguration): List<String>
}