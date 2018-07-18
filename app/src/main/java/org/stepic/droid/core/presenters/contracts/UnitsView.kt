package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.structure.Lesson
import org.stepik.android.model.structure.Progress
import org.stepik.android.model.structure.Unit

interface UnitsView {

    fun onEmptyUnits()

    fun onNeedShowUnits(unitList: List<Unit>, lessonList: List<Lesson>, progressMap: Map<Long, Progress>) // it is not mutable!

    fun onLoading()

    fun onConnectionProblem()
}
