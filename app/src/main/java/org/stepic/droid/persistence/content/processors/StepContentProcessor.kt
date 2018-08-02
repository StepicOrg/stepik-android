package org.stepic.droid.persistence.content.processors

import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.model.Step

interface StepContentProcessor {
    fun extractDownloadableContent(step: Step, configuration: DownloadConfiguration): Set<String>
    fun injectPersistentContent(stepWrapper: StepPersistentWrapper, links: Map<String, String>): StepPersistentWrapper
}