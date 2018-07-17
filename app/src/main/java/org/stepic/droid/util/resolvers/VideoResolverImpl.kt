package org.stepic.droid.util.resolvers

import android.support.annotation.AnyThread
import org.stepic.droid.analytic.Analytic
import org.stepik.android.model.structure.Video
import org.stepik.android.model.structure.VideoUrl
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.greaterThanMaxQuality
import javax.inject.Inject

class VideoResolverImpl
@Inject constructor(
        private val userPreferences: UserPreferences,
        private val analytic: Analytic)
    : VideoResolver {


    @AnyThread
    override fun resolveVideoUrl(video: Video?, isForPlaying: Boolean): String? {
        val urls = video?.urls ?: return null
        if (urls.isEmpty()) return null

        if (urls.size == 1) {
            return urls[0].url
        }

        return resolveFromWeb(urls, isForPlaying)
    }

    private fun resolveFromWeb(urlList: List<VideoUrl>, isForPlaying: Boolean): String? {
        var resolvedURL: String? = null

        try {
            val weWant = Integer.parseInt(
                    if (isForPlaying) {
                        userPreferences.qualityVideoForPlaying
                    } else {
                        userPreferences.qualityVideo
                    }
            )
            var bestDelta = Integer.MAX_VALUE
            urlList
                    .filter { !it.greaterThanMaxQuality() }
                    .forEach {
                        val currentQuality = Integer.parseInt(it.quality)
                        val qualityDelta = Math.abs(currentQuality - weWant)
                        if (qualityDelta < bestDelta) {
                            bestDelta = qualityDelta
                            resolvedURL = it.url
                        }
                    }
        } catch (e: NumberFormatException) {
            //this is approach in BAD case
            analytic.reportError(Analytic.Error.VIDEO_RESOLVER_FAILED, e)
            resolvedURL = fallbackOnQualityNotParsed(urlList)
        }

        return resolvedURL

    }

    private fun fallbackOnQualityNotParsed(urlList: List<VideoUrl>): String? {
        if (urlList.isEmpty()) {
            return null
        }
        return urlList.last().url
    }

}
