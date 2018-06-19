package org.stepic.droid.model.achievements

class AchievementFlatItem(
        currentLevelAchievement: Achievement?,
        nextLevelAchievement: Achievement,
        nextLevelAchievementProgress: AchievementProgress,

        val currentLevel: Int,
        val maxLevel: Int
) {
    val iconId: Long? = null // currentLevelAchievement?.iconId

    val isLocked = currentLevelAchievement == null

    val kind = nextLevelAchievement.kind
    val currentScore = nextLevelAchievementProgress.score
    val targetScore = nextLevelAchievement.targetScore

    override fun toString(): String {
        return "AchievementFlatItem(currentLevel=$currentLevel, maxLevel=$maxLevel, iconId=$iconId, kind='$kind', currentScore=$currentScore, targetScore=$targetScore)"
    }
}