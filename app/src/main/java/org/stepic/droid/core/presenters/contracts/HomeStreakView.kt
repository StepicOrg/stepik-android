package org.stepic.droid.core.presenters.contracts


interface HomeStreakView {
    fun showStreak(streak: Int)
    fun onEmptyStreak()
}