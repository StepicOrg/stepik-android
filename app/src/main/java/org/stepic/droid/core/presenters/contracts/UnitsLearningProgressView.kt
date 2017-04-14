package org.stepic.droid.core.presenters.contracts

interface UnitsLearningProgressView {

    fun setNewScore(unitId: Long, newScore: Double)

    fun setUnitPassed(unitId: Long)
}
