package org.stepik.android.cache.video.structure

object VideoUrlDbScheme {
    const val TABLE_NAME = "video_urls"

    object Columns {
        const val VIDEO_ID = "video_id"
        const val URL = "url"
        const val QUALITY = "quality"
    }
}