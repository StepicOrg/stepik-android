package org.stepic.droid.model

data class AppInfo(
        var custom_version: Int,
        var download_links: List<DownloadLink>?,
        var is_app_in_gp: Boolean
)