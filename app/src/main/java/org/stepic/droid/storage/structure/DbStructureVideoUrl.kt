package org.stepic.droid.storage.structure

object DbStructureVideoUrl {
    val savedVideosName = "savedVideoUrls"
    val externalVideosName = "externalVideoUrls"

    object Column {
        val videoId = "videoId"
        val quality = "quality"
        val url = "url";
    }
}
