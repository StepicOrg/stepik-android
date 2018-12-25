package org.stepic.droid.core.presenters

import org.stepic.droid.core.LocalProgressManager
import org.stepic.droid.core.presenters.contracts.UnitsLearningProgressView
import javax.inject.Inject


class UnitsLearningProgressPresenter
@Inject constructor() : PresenterBase<UnitsLearningProgressView>(), LocalProgressManager.UnitProgressListener {

    override fun onUnitPassed(unitId: Long) {
        view?.setUnitPassed(unitId)
    }

    override fun onScoreUpdated(unitId: Long, newScore: Double) {
        view?.setNewScore(unitId, newScore)
    }

}
