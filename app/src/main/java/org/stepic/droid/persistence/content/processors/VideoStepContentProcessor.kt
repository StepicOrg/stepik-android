package org.stepic.droid.persistence.content.processors

import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.resolvers.getUrlForVideoQuality
import org.stepik.android.model.Step
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import javax.inject.Inject

@PersistenceScope
class VideoStepContentProcessor
@Inject
constructor(): StepContentProcessor {
    override fun extractDownloadableContent(step: Step, configuration: DownloadConfiguration): Set<String> {
        val video = step.block?.video
        val url = video?.urls?.let { getUrlForVideoQuality(it, configuration.videoQuality) }
        val thumbnail = video?.thumbnail

        return if (url != null && thumbnail != null) {
            setOf(url, thumbnail)
        } else {
            emptySet()
        }
    }

    override fun injectPersistentContent(stepWrapper: StepPersistentWrapper, links: Map<String, String>): StepPersistentWrapper {
        val video = stepWrapper.step.block?.video ?: return stepWrapper
        val thumbnail = video.thumbnail?.let { links[it] ?: it } ?: return stepWrapper

        val urls = video.urls?.mapNotNull {
            val url = it.url?.let(links::get) ?: return@mapNotNull null
            val quality = it.quality ?: return@mapNotNull null
            VideoUrl(url, quality)
        }?.takeIf(List<VideoUrl>::isNotEmpty) ?: return stepWrapper

        val cachedVideo = Video(
                id = video.id,
                duration = video.duration,
                thumbnail = thumbnail,
                urls = urls
        )

        return stepWrapper.copy(cachedVideo = cachedVideo)
    }
}