package org.stepic.droid.storage.structure

object DbStructureVideoUrl {
    val tableName = "videoUrl"

    object Column {
        val videoId = "videoId"
        val url = "url";
        val quality = "quality"
    }
}
