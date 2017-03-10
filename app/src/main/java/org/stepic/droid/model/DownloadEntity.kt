package org.stepic.droid.model

data class DownloadEntity(val downloadId: Long,
                          val stepId: Long,
                          val videoId: Long,
                          val thumbnail: String?,
                          val quality: String?)
