package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Unit

interface UnitsView {

    fun onEmptyUnits()

    fun onLoading()

    fun onConnectionProblem()

    fun showDownloadProgress(progress: DownloadProgress)
    fun showVideoQualityDialog(position: Int)
    fun showOnRemoveDownloadDialog(position: Int)
    fun determineNetworkTypeAndLoad(position: Int)
}
