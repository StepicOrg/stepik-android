package org.stepic.droid.util.resolvers

import org.stepic.droid.util.greaterThanMaxQuality
import org.stepik.android.model.VideoUrl

fun getUrlForVideoQuality(urls: List<VideoUrl>, quality: String): String? = try {
    val weWant = Integer.parseInt(quality)
    var resolvedURL: String? = null
    var bestDelta = Integer.MAX_VALUE
    urls
            .filter { !it.greaterThanMaxQuality() }
            .forEach {
                val currentQuality = Integer.parseInt(it.quality)
                val qualityDelta = Math.abs(currentQuality - weWant)
                if (qualityDelta < bestDelta) {
                    bestDelta = qualityDelta
                    resolvedURL = it.url
                }
            }
    resolvedURL
} catch (_: NumberFormatException) {
    urls.lastOrNull()?.url
}