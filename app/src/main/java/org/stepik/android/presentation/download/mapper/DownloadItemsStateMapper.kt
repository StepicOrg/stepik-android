package org.stepik.android.presentation.download.mapper

import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.DownloadProgress
import ru.nobird.android.core.model.mutate
import org.stepik.android.presentation.download.DownloadView
import javax.inject.Inject

class DownloadItemsStateMapper
@Inject
constructor() {
    fun replaceDownloadItem(state: DownloadView.State, downloadItem: DownloadItem): DownloadView.State {
        val downloadedCourses = if (state is DownloadView.State.DownloadedCoursesLoaded) {
            state.courses
        } else {
            emptyList()
        }

        val itemIndex = downloadedCourses.binarySearch { it.course.id.compareTo(downloadItem.course.id) }

        val courses = downloadedCourses.mutate {
            if (downloadItem.status is DownloadProgress.Status.Cached) {
                if (itemIndex < 0) {
                    add(-itemIndex - 1, downloadItem)
                } else {
                    set(itemIndex, downloadItem)
                }
            } else {
                if (itemIndex > -1) {
                    removeAt(itemIndex)
                }
            }
        }
        return if (courses.isEmpty()) {
            DownloadView.State.Empty
        } else {
            DownloadView.State.DownloadedCoursesLoaded(courses)
        }
    }
}