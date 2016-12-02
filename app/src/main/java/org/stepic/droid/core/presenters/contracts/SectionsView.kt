package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Section
import viewmodel.ProgressViewModel

interface SectionsView {

    fun onEmptySections()

    fun onConnectionProblem()

    fun onNeedShowSections(sectionList: List<Section>, progressMap: Map <String, ProgressViewModel>)

    fun onLoading()

}
