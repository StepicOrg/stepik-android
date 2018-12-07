package org.stepic.droid.storage.structure

@Deprecated("Use VideoDao instead")
object DbStructureVideoUrl {
    const val externalVideosName = "externalVideoUrls"

    object Column {
        const val videoId = "videoId"
        const val quality = "quality"
        const val url = "url"
    }
}
