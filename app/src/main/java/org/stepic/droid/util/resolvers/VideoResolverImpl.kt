package org.stepic.droid.util.resolvers

import android.support.annotation.AnyThread
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import org.stepic.droid.preferences.UserPreferences
import javax.inject.Inject

class VideoResolverImpl
@Inject constructor(
    private val userPreferences: UserPreferences
) : VideoResolver {

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
        val quality = if (isForPlaying) {
            userPreferences.qualityVideoForPlaying
        } else {
            userPreferences.qualityVideo
        }
        return getUrlForVideoQuality(urlList, quality)
    }

}
