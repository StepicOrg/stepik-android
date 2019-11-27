package org.stepik.android.domain.download.interactor

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadItem
import org.stepik.android.domain.download.repository.DownloadRepository
import javax.inject.Inject

class DownloadsInteractor
@Inject
constructor(
    private val downloadRepository: DownloadRepository
) {
    fun fetchDownloadItems(): Observable<DownloadItem> =
        downloadRepository.getDownloads()
}