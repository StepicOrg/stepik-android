package org.stepic.droid.persistence.content.processors

import org.jsoup.Jsoup
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.model.Step
import javax.inject.Inject

@PersistenceScope
class ImageStepContentProcessor
@Inject
constructor() : StepContentProcessor {
    companion object {
        private const val IMG_TAG = "img"
        private const val SRC_ATTR = "src"
    }

    override fun extractDownloadableContent(step: Step, configuration: DownloadConfiguration): Set<String> {
        val stepText = step.block?.text
            ?: return emptySet()

        return Jsoup.parse(stepText)
            .select("$IMG_TAG[$SRC_ATTR]")
            .mapNotNull { it.attr(SRC_ATTR) }
            .toSet()
    }

    override fun injectPersistentContent(stepWrapper: StepPersistentWrapper, links: Map<String, String>): StepPersistentWrapper {
        val block = stepWrapper.step.block
            ?: return stepWrapper

        val document = block.text
            ?.let(Jsoup::parse)
            ?: return stepWrapper

        document.select("$IMG_TAG[$SRC_ATTR]").forEach { element ->
            val src = element.attr(SRC_ATTR)
            val link = links[src]?.let { "file://$it" }
            element.attr(SRC_ATTR, link ?: src)
        }

        return stepWrapper.copy(step = stepWrapper.step.copy(block = block.copy(text = document.toString())))
    }
}