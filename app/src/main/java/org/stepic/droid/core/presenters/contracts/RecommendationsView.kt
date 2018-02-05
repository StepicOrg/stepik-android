package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.adaptive.ui.adapters.QuizCardsAdapter


interface RecommendationsView {
    fun onAdapter(cardsAdapter: QuizCardsAdapter)

    fun onLoading()
    fun onConnectivityError()
    fun onRequestError()

    fun onCourseNotSupported()
    fun onCourseCompleted()
    fun onCardLoaded()

    // Gamification
    fun updateExp(
            exp: Long,
            currentLevelExp: Long,
            nextLevelExp: Long,

            level: Long)
    fun onStreak(streak: Long)
    fun onStreakLost()
//    fun onStreakRestored()

//    fun showDailyRewardDialog(progress: Long)
    fun showNewLevelDialog(level: Long)
    fun showExpTooltip()
//    fun showRateAppDialog()
//    fun showStreakRestoreDialog(streak: Long)
}